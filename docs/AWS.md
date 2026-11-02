# Configurar a AWS para o CI/CD

O workflow [`.github/workflows/cd.yml`](../.github/workflows/cd.yml) publica a imagem da API no Amazon ECR. O GitHub entra na conta por OIDC e assume uma role. Não é preciso criar access key.

Use a região **us-east-1** em todos os passos. Se escolher outra, troque `us-east-1` na policy e na variável `AWS_REGION`.

Anote antes de começar:

| O quê | Onde achar | Exemplo |
|---|---|---|
| ID da conta | Canto superior direito do console AWS, ao lado do seu usuário | `123456789012` |
| Dono e repositório | URL do GitHub: `github.com/<dono>/<repositorio>` | `fiap-group123/auto-repair-shop` |

O repositório ECR `auto-repair-shop` é criado pelo próprio workflow na primeira publicação.

## 1. Provedor OpenID Connect

1. No console AWS, abra **IAM**.
2. No menu esquerdo, **Identity providers**.
3. **Add provider**.
4. **Provider type:** OpenID Connect.
5. **Provider URL:** `https://token.actions.githubusercontent.com`
6. **Audience:** `sts.amazonaws.com`
7. **Add provider**.

Se esse provedor já aparecer na lista, não crie outro.

## 2. Policy de permissão do ECR

1. **IAM** → **Policies** → **Create policy**.
2. Abra a aba **JSON**, apague o conteúdo e cole o JSON abaixo.
3. Troque `<conta>` pelo ID de 12 dígitos.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "EcrLogin",
      "Effect": "Allow",
      "Action": "ecr:GetAuthorizationToken",
      "Resource": "*"
    },
    {
      "Sid": "EcrPush",
      "Effect": "Allow",
      "Action": [
        "ecr:DescribeRepositories",
        "ecr:CreateRepository",
        "ecr:BatchCheckLayerAvailability",
        "ecr:CompleteLayerUpload",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:PutImage",
        "ecr:BatchGetImage"
      ],
      "Resource": "arn:aws:ecr:us-east-1:<conta>:repository/auto-repair-shop"
    },
    {
      "Sid": "TerraformStateList",
      "Effect": "Allow",
      "Action": ["s3:ListBucket"],
      "Resource": "arn:aws:s3:::fiap-group123-auto-repair-shop-tfstate"
    },
    {
      "Sid": "TerraformStateObjects",
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject"
      ],
      "Resource": "arn:aws:s3:::fiap-group123-auto-repair-shop-tfstate/*"
    },
    {
      "Sid": "TerraformStateBucket",
      "Effect": "Allow",
      "Action": [
        "s3:CreateBucket",
        "s3:PutBucketVersioning",
        "s3:PutEncryptionConfiguration",
        "s3:PutBucketPublicAccessBlock"
      ],
      "Resource": "arn:aws:s3:::fiap-group123-auto-repair-shop-tfstate"
    }
  ]
}
```

4. **Next**.
5. **Policy name:** `github-auto-repair-shop-ecr`
6. **Create policy**.

## 3. Role do GitHub

1. **IAM** → **Roles** → **Create role**.
2. **Trusted entity type:** Web identity.
3. **Identity provider:** `token.actions.githubusercontent.com`
4. **Audience:** `sts.amazonaws.com`
5. **Next**. O GitHub pede o repositório nessa tela; pode preencher o dono e o nome. A relação de confiança será substituída no passo 4.
6. Marque a policy `github-auto-repair-shop-ecr`.
7. **Next**.
8. **Role name:** `github-auto-repair-shop`
9. **Create role**.

## 4. Relação de confiança

A role só pode ser assumida pelo repositório certo.

1. Abra a role `github-auto-repair-shop`.
2. Aba **Trust relationships** → **Edit trust policy**.
3. Substitua o JSON. Troque `<conta>`, `<dono>` e `<repositorio>`.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::<conta>:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:<dono>/<repositorio>:*"
        }
      }
    }
  ]
}
```

4. **Update policy**.
5. No topo da role, copie o **ARN**. O formato é `arn:aws:iam::<conta>:role/github-auto-repair-shop`.

## 5. Levar os valores para o GitHub

No repositório: **Settings** → **Secrets and variables** → **Actions**.

**Variables** → **New repository variable**:

| Nome | Valor |
|---|---|
| `JAVA_VERSION` | `17` |
| `GRADLE_VERSION` | `9.5.1` |
| `AWS_REGION` | `us-east-1` |

**Secrets** → **New repository secret**:

| Nome | Valor |
|---|---|
| `AWS_ROLE_ARN` | ARN copiado no passo 4 |

Não crie access key da AWS no GitHub. A variável `ECR_REPOSITORY` só é necessária se o repositório na AWS tiver outro nome. Sem ela, o workflow usa `auto-repair-shop`.

## 6. Conferir

1. Faça push na `main` ou abra **Actions** → **CD** → **Run workflow**.
2. Espere o job `deliver` terminar.
3. No console AWS, abra **ECR** → **Repositories** → `auto-repair-shop`.
4. A imagem deve ter a tag `sha-` seguida de 12 caracteres do commit. Na `main`, também aparece `latest`.

O banco não é criado na AWS. O PostgreSQL sobe só no cluster kind do job e some quando o runner termina.

## 7. Bucket do state do Terraform

O `terraform.tfstate` do cluster não entra no Git. O `.gitignore` já ignora `*.tfstate`, `*.tfstate.*` e `*.tfstate.backup`.

O state remoto fica em `s3://fiap-group123-auto-repair-shop-tfstate/auto-repair-shop/terraform.tfstate`. O bucket bloqueia acesso público, versiona os objetos e cifra com AES256. O workflow cria o bucket no passo **Ensure Terraform state bucket**, se a policy da função já tiver as permissões `s3:` da seção 2.

Se a policy foi criada antes dessas permissões, abra **IAM** → **Policies** → `github-auto-repair-shop-ecr` → **Edit** e cole de novo o JSON da seção 2.

Para criar o bucket na sua máquina, com a AWS autenticada:

```bash
bash scripts/ensure-tfstate-bucket.sh
cd infra
terraform init
```

`infra/bootstrap` descreve o mesmo bucket. Use esse diretório só se o bucket ainda não existir. Se o workflow já tiver criado, não rode o apply de novo.
