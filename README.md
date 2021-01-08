# fix_me

### Needed

apt install default-jdk
apt install maven
mysql

### Installation

Install the db
cd parent-project
mvn clean package

java -jar ./router/target/router
java -jar ./broker/target/broker
java -jar ./market/target/market

### Input

Type of message (SELL or BUY)
TargetId (number)
InstrumentId (number)
Quantity (number)

### validation FIX message
Fix_message validator : https://drewnoakes.com/fix-decoder/#
