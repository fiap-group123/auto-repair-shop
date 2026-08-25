import os
import re
import xml.etree.ElementTree as ET

with open("build/reports/kover/report.xml", encoding="utf-8") as report:
    report_xml = re.sub(r"<!DOCTYPE[^>]*>", "", report.read(), count=1)

root = ET.fromstring(report_xml)
line = next(counter for counter in root.findall("counter") if counter.get("type") == "LINE")
covered = int(line.get("covered"))
missed = int(line.get("missed"))
total = covered + missed
percentage = (covered / total * 100) if total else 0.0
formatted = f"{percentage:.2f}"

with open(os.environ["GITHUB_OUTPUT"], "a", encoding="utf-8") as output:
    output.write(f"percentage={formatted}\n")

print(f"Line coverage: {formatted}%")