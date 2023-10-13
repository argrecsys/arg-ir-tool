# Arguments-enhanced IR

Argument-enhanced information retrieval project repository. A case study in the <a href="https://decide.madrid.es" target="_blank">Decide Madrid</a> database.

We present a tool that not only allows to retrieve argumentative information, but also to validate the returned arguments. The search runs on Apache Lucene and the results (proposals and comments) are re-ranked according to the number of arguments they have.

This project takes advantage of the arguments previously extracted (from the citizen proposals of the Decide Madrid platform) in the <a  href="https://github.com/argrecsys/arg-miner" target="_blank">argrecsys/arg-miner</a> repository.

## Configuration
The input parameters (<a href="https://github.com/argrecsys/arg-ir-tool/blob/main/code/ArgumentIR/Resources/config/params.json">params.json</a> file) of the tool are:
```json
{
    "language": "es",
    "data_path": "../../data"
}
```

## Documentation
Please read the [contributing](https://github.com/argrecsys/arg-ir-tool/blob/main/docs/CONTRIBUTING.md) and [code of conduct](https://github.com/argrecsys/arg-ir-tool/blob/main/docs/CODE_OF_CONDUCT.md) documentation.

## Authors
Created on Jan 25, 2022  
Created by:
- <a href="https://github.com/ansegura7" target="_blank">Andrés Segura-Tinoco</a>
- <a href="http://arantxa.ii.uam.es/~cantador/" target="_blank">Iv&aacute;n Cantador</a>

## License
This project is licensed under the terms of the <a href="https://github.com/argrecsys/arg-enhanced-ir/blob/main/LICENSE">Apache License 2.0</a>.

## Acknowledgements
This work was supported by the Spanish Ministry of Science and Innovation (PID2019-108965GB-I00).
