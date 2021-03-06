#!/usr/bin/env bash
# Licensed to ObjectStyle LLC under one or more
# contributor license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ObjectStyle LLC licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

GRAALVM_VERSION=20.2.0
curl -OL https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-${GRAALVM_VERSION}/graalvm-ce-java8-linux-amd64-${GRAALVM_VERSION}.tar.gz
tar zxf graalvm-ce-java8-linux-amd64-${GRAALVM_VERSION}.tar.gz || exit 1
mkdir /usr/lib/jvm/ graalvm-ce-java8-${GRAALVM_VERSION}
sudo mv graalvm-ce-java8-${GRAALVM_VERSION} /usr/lib/jvm/graalvm-ce-java8-${GRAALVM_VERSION}/
/usr/libexec/java_home -v 1.8
export JAVA_HOME=/usr/lib/jvm/graalvm-ce-java8-${GRAALVM_VERSION}/
export PATH=${JAVA_HOME}/bin:$PATH
${JAVA_HOME}/bin/gu install native-image

mvn package -Pnative-image,assembly-mac || exit 1
