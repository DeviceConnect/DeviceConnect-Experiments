import argparse
import xml.etree.ElementTree as ET
import re
import os

def updatePOM(pomFile, version):
    ET.register_namespace('', 'http://maven.apache.org/POM/4.0.0')
    tree = ET.parse(pomFile)
    root = tree.getroot()
    versionElement = root.find('./pom:version', {'pom':'http://maven.apache.org/POM/4.0.0'})
    versionElement.text = version
    tree.write(pomFile, encoding="UTF-8")
    print 'Changed: ' + pomFile

def updateREADME(filename, version):
    with open(filename, 'r') as file:
        filedata = file.read()
    
    repl = '\g<1>' + version + '\g<2>'
    filedata = re.sub(r'(deviceconnect-codegen-project-).+(-dist\.zip)', repl, filedata)
    filedata = re.sub(r'(https://github.com/.+/DeviceConnect-Experiments/releases/tag/codegen-v).+(\))', repl, filedata)


    with open(filename, 'w') as file:
        file.write(filedata)
    print 'Changed: ' + filename

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("version", type=str, help="version name of deviceconnect-codegen")
    args = parser.parse_args()

    baseDir = os.path.dirname(os.path.abspath(__file__))
    updatePOM(os.path.realpath(baseDir + '/../pom.xml'), args.version)
    updatePOM(os.path.realpath(baseDir + '/../modules/deviceconnect-codegen/pom.xml'), args.version)
    updateREADME(os.path.realpath(baseDir + '/../README.md'), args.version)
    print 'Completed'

if __name__ == "__main__":
    main()
