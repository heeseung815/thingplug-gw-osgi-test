# build
sbt osgiBundle

# karaf 환경 설정
( 참고 https://stackoverflow.com/questions/19820852/simple-hello-world-demonstrating-osgi-with-akka-actors )
org.osgi.framework.system.packages.extra 에
sun.net 추가
( 참고  : https://gist.github.com/michalskalski/36f2c21d52d28f7bf107 )
org.osgi.framework.system.packages.extra=org.apache.karaf.branding,sun.reflect,sun.reflect.misc,sun.misc,sun.nio.ch

# 실행
cd $KARAF_HOME
bin/karaf

install mvn:org.scala-lang/scala-library/2.12.2
install mvn:org.scala-lang/scala-reflect/2.12.2
install mvn:org.scala-lang.modules/scala-java8-compat_2.12
install mvn:org.scala-lang.modules/scala-parser-combinators_2.12
install mvn:com.fasterxml.jackson.core/jackson-annotations/2.8.8
install mvn:com.fasterxml.jackson.core/jackson-core/2.8.8
install mvn:com.fasterxml.jackson.core/jackson-databind/2.8.8
install mvn:com.fasterxml.jackson.datatype/jackson-datatype-jdk8/2.8.8
install mvn:com.fasterxml.jackson.datatype/jackson-datatype-jsr310/2.8.8
install mvn:com.fasterxml.jackson.module/jackson-module-jaxb-annotations/2.8.8
install mvn:com.typesafe/config/1.3.1
install mvn:com.typesafe/ssl-config-core_2.12/0.2.1
install mvn:com.typesafe.akka/akka-actor_2.12/2.5.4
install mvn:com.typesafe.akka/akka-osgi_2.12/2.5.4
install mvn:com.typesafe.akka/akka-protobuf_2.12/2.5.4
install mvn:com.typesafe.akka/akka-remote_2.12/2.5.4
install mvn:org.reactivestreams/reactive-streams/1.0.1
install mvn:com.typesafe.akka/akka-stream_2.12/2.5.4
install mvn:com.typesafe.scala-logging/scala-logging_2.12/3.5.0
install mvn:io.aeron/aeron-client/1.2.5
install mvn:io.aeron/aeron-driver/1.2.5
#install mvn:io.netty/netty/3.10.6.Final
install mvn:io.spray/spray-json_2.12/1.3.3
install mvn:joda-time/joda-time/2.9.9
install mvn:org.agrona/agrona/0.9.5
install mvn:org.eclipse.paho/org.eclipse.paho.client.mqttv3/1.1.0
install mvn:org.osgi/org.osgi.compendium/4.3.1
install mvn:org.osgi/org.osgi.core/4.3.1
install file:///Users/hscho/uangel/projects/test/thingplug2-gw-osgi-test/lib/akka-stream-alpakka-mqtt_2.12-0.10.jar
install file:///Users/hscho/uangel/projects/test/thingplug2-gw-osgi-test/lib/onem2m.jar
install file:///Users/hscho/uangel/projects/test/thingplug2-gw-osgi-test/common/target/scala-2.12/gwcommon_2.12-2.0.0-SNAPSHOT.jar
install file:///Users/hscho/uangel/projects/test/thingplug2-gw-osgi-test/module/import_lora/target/scala-2.12/import_lora_2.12-2.0.0-SNAPSHOT.jar
install file:///Users/hscho/uangel/projects/test/thingplug2-gw-osgi-test/module/flow_preprocessing_current/target/scala-2.12/flow_preprocessing_current_2.12-2.0.0-SNAPSHOT.jar
install file:///Users/hscho/uangel/projects/test/thingplug2-gw-osgi-test/module/flow_preprocessing_vib/target/scala-2.12/flow_preprocessing_vib_2.12-2.0.0-SNAPSHOT.jar
install file:///Users/hscho/uangel/projects/test/thingplug2-gw-osgi-test/module/export_thingplug/target/scala-2.12/export_thingplug_2.12-2.0.0-SNAPSHOT.jar
install file:///Users/hscho/uangel/projects/test/thingplug2-gw-osgi-test/module/export_datalake/target/scala-2.12/export_datalake_2.12-2.0.0-SNAPSHOT.jar
install file:///Users/hscho/uangel/projects/test/thingplug2-gw-osgi-test/main/target/scala-2.12/gateway_2.12-2.0.0-SNAPSHOT.jar
install file:///Users/hscho/uangel/projects/test/thingplug2-gw-osgi-test/module/main_test/target/scala-2.12/main_test_2.12-2.0.0-SNAPSHOT.jar

// for macbook
install file:///Users/hscho/std/thingplug-gw-osgi-test/lib/akka-stream-alpakka-mqtt_2.12-0.10.jar
install file:///Users/hscho/std/thingplug-gw-osgi-test/lib/onem2m.jar
install file:///Users/hscho/std/thingplug-gw-osgi-test/common/target/scala-2.12/gwcommon_2.12-2.0.0-SNAPSHOT.jar
install file:///Users/hscho/std/thingplug-gw-osgi-test/module/import_lora/target/scala-2.12/import_lora_2.12-2.0.0-SNAPSHOT.jar
install file:///Users/hscho/std/thingplug-gw-osgi-test/module/flow_preprocessing_current/target/scala-2.12/flow_preprocessing_current_2.12-2.0.0-SNAPSHOT.jar
install file:///Users/hscho/std/thingplug-gw-osgi-test/module/flow_preprocessing_vib/target/scala-2.12/flow_preprocessing_vib_2.12-2.0.0-SNAPSHOT.jar
install file:///Users/hscho/std/thingplug-gw-osgi-test/module/export_thingplug/target/scala-2.12/export_thingplug_2.12-2.0.0-SNAPSHOT.jar
install file:///Users/hscho/std/thingplug-gw-osgi-test/module/export_datalake/target/scala-2.12/export_datalake_2.12-2.0.0-SNAPSHOT.jar
install file:///Users/hscho/std/thingplug-gw-osgi-test/main/target/scala-2.12/gateway_2.12-2.0.0-SNAPSHOT.jar
install file:///Users/hscho/std/thingplug-gw-osgi-test/module/main_test/target/scala-2.12/main_test_2.12-2.0.0-SNAPSHOT.jar


#install --r3-bundles mvn:com.typesafe.play/play-functional_2.12/2.6.0
#install --r3-bundles mvn:com.typesafe.play/play-json_2.12/2.6.0
#install mvn:log4j/log4j/1.2.17
#install mvn:org.slf4j/slf4j-log4j12
#install mvn:org.slf4j/slf4j-api/1.7.21


start 모든 bundle
start 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87
