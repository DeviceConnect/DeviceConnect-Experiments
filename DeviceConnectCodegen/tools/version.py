import argparse
import xml.etree.ElementTree as ET
import re

def updatePOM(pomFile, version):
    ET.register_namespace('', 'http://maven.apache.org/POM/4.0.0')
    tree = ET.parse(pomFile)
    root = tree.getroot()
    versionElement = root.find('./pom:version', {'pom':'http://maven.apache.org/POM/4.0.0'})
    versionElement.text = version
    tree.write(pomFile, encoding="UTF-8")

def updateREADME(filename, version):
    with open(filename, 'r') as file:
        filedata = file.read()
    
    repl = '\g<1>' + version + '\g<2>'
    filedata = re.sub(r'(deviceconnect-codegen-project-).+(-dist\.zip)', repl, filedata)
    filedata = re.sub(r'(https://github.com/.+/DeviceConnect-Experiments/releases/tag/codegen-v).+(\))', repl, filedata)

    with open(filename, 'w') as file:
        file.write(filedata)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("version", type=str, help="version name of deviceconnect-codegen")
    args = parser.parse_args()

    updatePOM('../pom.xml', args.version)
    updatePOM('../modules/deviceconnect-codegen/pom.xml', args.version)
    updateREADME('../README.md', args.version)

if __name__ == "__main__":
    main()
