#!/usr/bin/python3

"""
Copyright 2018 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

import argparse, os, subprocess, sys

NAME_HELP = '''
  The name of the artifact you want to add to the prebuilts folder.
  E.g. android.arch.work:work-runtime-ktx:1.0.0-alpha07
'''

ANDROIDX_BUILD_ID_HELP = '''
  The build id of https://ci.android.com/builds/branches/aosp-androidx-main/grid?
  to use for fetching androidx prebuilts.
'''

METALAVA_BUILD_ID_HELP = '''
  The build id of https://ci.android.com/builds/branches/aosp-metalava-master/grid?
  to use for metalava prebuilt fetching.
'''

ALLOW_JETBRAINS_DEV_HELP = '''
  Whether or not to allow artifacts to be fetched from Jetbrains' dev repository
  E.g. https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev
'''

FETCH_KMP_ARTIFACTS_HELP = '''
  If set, we'll fetch all KMP artifacts as well
  E.g. passing -n "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1" will fetch native targets
  of coroutines-core as well.
'''

if sys.version_info[0] < 3: raise Exception("Python 2 is not supported by this script. If your system python calls python 2 after python 2 end-of-life on Jan 1 2020, you should probably change it.")

def main():
    """Parses the command line arguments, and executes the gradle script
    which downloads the maven artifacts.
    """
    os.chdir(os.path.dirname(sys.argv[0]))
    parser = argparse.ArgumentParser(
        description='Helps download maven artifacts to prebuilts.')
    parser.add_argument('-n', '--name', help=NAME_HELP,
                        required=True, dest='name')
    parser.add_argument('-ab', '--androidx-build-id', help=ANDROIDX_BUILD_ID_HELP,
                        required=False, dest='androidx_build_id')
    parser.add_argument('-mb', '--metalava-build-id', help=METALAVA_BUILD_ID_HELP,
                        required=False, dest='metalava_build_id')
    parser.add_argument('-ajd', '--allow-jetbrains-dev', help=ALLOW_JETBRAINS_DEV_HELP,
                        required=False, action='store_true')
    parser.add_argument('-kmp', '--fetch-kmp-artifacts', help=FETCH_KMP_ARTIFACTS_HELP,
                        required=False, action='store_true')
    parse_result = parser.parse_args()
    artifact_name = parse_result.name
    if ("kotlin-native-linux" in artifact_name or "kotlin-native-prebuilt-linux" in artifact_name):
        artifact_name = fix_kotlin_native(artifact_name)

    # Add -Dorg.gradle.debug=true to debug or --stacktrace to see the stack trace
    command = './gradlew --build-file build.gradle.kts --no-configuration-cache -PartifactNames=%s' % (
        artifact_name)
    # AndroidX Build Id
    androidx_build_id = parse_result.androidx_build_id
    if (androidx_build_id):
      command = command + ' -PandroidxBuildId=%s' % (androidx_build_id)
    # Metalava Build Id
    metalava_build_id = parse_result.metalava_build_id
    if (metalava_build_id):
      command = command + ' -PmetalavaBuildId=%s' % (metalava_build_id)
    if (parse_result.allow_jetbrains_dev):
      command = command + ' -PallowJetbrainsDev'
    if (parse_result.fetch_kmp_artifacts):
      command = command + ' -PkmpBuild=true'
    print(command)
    
    process = subprocess.Popen(command,
                               shell=True,
                               stdin=subprocess.PIPE)
    process.communicate()
    assert not process.returncode

    # TODO(b/223642687) automate the updating of signature information
    COLOR_YELLOW="\u001B[33m"
    COLOR_CLEAR="\u001B[0m"
    print("")
    print(COLOR_YELLOW + "You may also need to update signature information. See gradle/README.md" + COLOR_CLEAR)
    print("")

    # Generate our own .pom file so Gradle will use this artifact without also checking the internet.
    # This can be removed once https://youtrack.jetbrains.com/issue/KT-35049 is resolved
    if ("kotlin-native-linux" in artifact_name):
      version = artifact_name.split("@")[0].split(":")[2]
      output_file = open(f"../../../.././prebuilts/androidx/external/no-group/kotlin-native-linux/{version}/kotlin-native-linux-{version}.pom", 'w+')
      output_file.write(f"""
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.jetbrains.kotlin</groupId>
  <artifactId>kotlin-native-linux</artifactId>
  <version>{version}</version>
  <name>kotlin-native-linux</name>
  <url>https://download.jetbrains.com/kotlin/native/builds</url>
</project>\n""")
      output_file.close()

#kotlin-native-linux has weird syntax requirements; needs to be :kotlin-native-linux:VERSION@tar.gz
def fix_kotlin_native(name_arg):
  if name_arg[0]!=":": name_arg = ":"+name_arg
  if not name_arg.endswith("@tar.gz"): name_arg += "@tar.gz"
  return name_arg

if __name__ == '__main__':
    main()
