# Arguments-enhanced IR
![version](https://img.shields.io/badge/version-1.1.0-blue)
![last-update](https://img.shields.io/badge/last_update-8/16/2022-orange)
![license](https://img.shields.io/badge/license-Apache_2.0-brightgreen)

ArgIR repository, a tool for annotation and retrieval of argumentative information from textual content. A case study in the <a href="https://decide.madrid.es" target="_blank">Decide Madrid</a> database.

We present a tool that not only allows to retrieve argumentative information, but also to annotate new arguments and/or validate them (in terms of their *topical relevance* and *rhetorical quality*). The search runs on <a href="https://lucene.apache.org/" target="_blank">Apache Lucene</a> and the results (proposals and comments) are re-ranked according to their level of controversy or the number and quality of arguments they have.

This project takes advantage of the arguments previously extracted (from the citizen proposals of the Decide Madrid platform) in the <a  href="https://github.com/argrecsys/arg-miner" target="_blank">argrecsys/arg-miner</a> repository.

## Papers
This work (v1.0) will be presented as a long paper at <a href="https://www.irit.fr/CIRCLE/">CIRCLE (Joint Conference of the Information Retrieval Communities in Europe) 2022</a>. CIRCLE 2022 will be hosted by the Université de Toulouse, France, 4-7th July 2022. A draft of the paper can be found <a href="https://github.com/argrecsys/arg-ir-tool/tree/main/papers/">here</a>.

## Screenshots
**Argument-enhanced Information Retrieval** tool: allows the retrieval of argumentative information from textual content.

![arg-ir-gui-main](https://raw.githubusercontent.com/argrecsys/arg-enhanced-ir/main/images/gui-main.gif)

**Arguments Annotation** form: allows manual annotation and validation of arguments.

![arg-ir-gui-annotation](https://raw.githubusercontent.com/argrecsys/arg-enhanced-ir/main/images/gui-annotation.gif)

## Annotation and validation
The tool allows you to annotate/edit arguments, as well as validate their relevance and quality.
Below is an example of the generated <a href="https://github.com/argrecsys/arg-enhanced-ir/blob/main/data/results/labels.csv" target="_blank">validation file</a>.

| proposal_id | argument_id | relevance | quality | timestamp | username |
| ----------- | ----------- | --------- | ------- | --------- | -------- |
| 7 | 7-85675-1-1 | VERY_RELEVANT | SUFFICIENT | 10/3/2022 20:53:00 | andres.segura |
| 1419 | 1419-30381-1-1 | RELEVANT | SUFFICIENT | 17/02/2022 23:04 | andres.segura |
| 2576 | 2576-0-1-1 | VERY_RELEVANT | HIGH_QUALITY | 16/02/2022 17:31 | andres.segura |
| 10996 | 10996-0-1-1 | VERY_RELEVANT | HIGH_QUALITY | 24/02/2022 20:12 | andres.segura |
| 26787 | 26787-204339-1-1 | NOT_RELEVANT | LOW_QUALITY | 2022-03-09 16:39:43 | andres.segura |

## Validation
As a preliminary offline evaluation, using the developed tool, we manually validated 20% of the arguments extracted by the simple syntactic pattern-based method. For the <em>topical relevance</em> metric, 8.6% of the arguments were labeled as <em>spam</em>, 36.9% as <em>not relevant</em>, 39.9% as <em>relevant</em>, and 14.6% as <em>very relevant</em>, whereas for the <em>rhetoric quality</em> metric, 42.3% of the arguments were of <em>low quality</em>, 40.6% of <em>sufficient quality</em>, and 17.1% of <em>high quality</em>. Although these results are modest, they can be considered acceptable as baseline values, taking into account they were obtained with a heuristic method that does not require training data and parameter tuning.

## Dependencies
The implemented solutions depend on or make use of the following libraries and .jar files:
- JDK 16
- <a href="https://lucene.apache.org/" target="_blank">Apache Lucene</a> 9.0
- MySQL Connector 8.0.22
- <a href="https://mongodb.github.io/mongo-java-driver/" target="_blank">MongoDB Java Driver</a> 3.12.10
- Snake YAML 1.9
- JSON Java 20210307
- OpenCSV 4.1

## Execution and Use
The project has an executable package in the `\jar` folder, called `ArgumentIR.jar`. To run the tool from the Command Prompt (CMD), execute the following commands:

``` console
  cd "arg-ir-tool\jar\"
  java -jar ArgumentIR.jar
```

## Authors
Created on Jan 25, 2022  
Created by:
- <a href="https://github.com/ansegura7" target="_blank">Andrés Segura-Tinoco</a>
- <a href="http://arantxa.ii.uam.es/~cantador/" target="_blank">Iv&aacute;n Cantador</a>

## License
This project is licensed under the terms of the <a href="https://github.com/argrecsys/arg-enhanced-ir/blob/main/LICENSE">Apache License 2.0</a>.

## Acknowledgements
This work was supported by the Spanish Ministry of Science and Innovation (PID2019-108965GB-I00).
