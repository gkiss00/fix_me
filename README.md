# fix_me

* Financial Information eXchange

## Goal

* Code a trading algorithm and play with the FIX protocol.
Every market and broker has a random list of items at the beginning of the simulation. Each time a broker try to sell or buy an item, a pre-validation is made. If it pass, the message is encrypted with the FIX protocol and forwarded to the router. The router send the message to the right target (market). The market check if he can perform the trade and send the response to the router. Then the router forward the response to the broker and the stock are updated. As many broker and market can be launched at any time of the simulation. All the brokers, markets and transactions are stored in the database. If a broker disconnect during a transaction, it is put to standby.
The broker can then reconnect passing his Id to the program.

### Needed

* apt install default-jdk
* apt install maven
* mysql

### Installation

* sudo mysql -u root
* alter user 'root'@'localhost' identified with mysql_native_password by 'root';
* Install the db
* cd parent-project
* mvn clean package

* java -jar ./router/target/router
* java -jar ./broker/target/broker
* java -jar ./market/target/market

### Input

* Type of message (SELL or BUY)
* TargetId (number)
* InstrumentId (number)
* Quantity (number)

### validation FIX message

* Fix_message validator : https://drewnoakes.com/fix-decoder/#
