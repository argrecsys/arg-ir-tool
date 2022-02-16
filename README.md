# Arguments-enhanced IR
![version](https://img.shields.io/badge/version-0.9.0-blue)
![last-update](https://img.shields.io/badge/last_update-2/16/2022-orange)
![license](https://img.shields.io/badge/license-Apache_2.0-brightgreen)

Argument-enhanced information retrieval project repository. A case study in the <a href="https://decide.madrid.es" target="_blank">Decide Madrid</a> database.

We present a tool that not only allows to retrieve argumentative information, but also to validate the returned arguments (in the categories: relevant, valid and invalid). The search runs on Apache Lucene and the results (proposals and comments) are re-ranked according to the number of arguments they have.

This project takes advantage of the arguments previously extracted (from the citizen proposals of the Decide Madrid platform) in the <a  href="https://github.com/argrecsys/arg-miner" target="_blank">argrecsys/arg-miner</a> repository.

## Screenshots
![arg-ir-gui](https://raw.githubusercontent.com/argrecsys/arg-enhanced-ir/main/images/gui.gif)

## Dependencies
The implemented solutions depend on or make use of the following libraries and .jar files:
- JDK 16
- <a href="https://lucene.apache.org/" target="_blank">Apache Lucene</a> 9.0
- MySQL Connector 8.0.22
- <a href="https://mongodb.github.io/mongo-java-driver/" target="_blank">MongoDB Java Driver</a> 3.12.10
- Snake YAML 1.9
- JSON Java 20210307

## Authors
Created on Jan 25, 2022  
Created by:
- <a href="https://github.com/ansegura7" target="_blank">Andrés Segura-Tinoco</a>
- <a href="http://arantxa.ii.uam.es/~cantador/" target="_blank">Iv&aacute;n Cantador</a>

## License
This project is licensed under the terms of the <a href="https://github.com/argrecsys/arg-enhanced-ir/blob/main/LICENSE">Apache License 2.0</a>.

## Acknowledgements
This work was supported by the Spanish Ministry of Science and Innovation (PID2019-108965GB-I00).
