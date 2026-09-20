# Working glossary

[Home](../README.md) · [LLM module](llm-components.md) · [Retrieval module](retrieval.md)

| Term                 | Meaning in this repository                                                             |
| -------------------- | -------------------------------------------------------------------------------------- |
| Agent                | A system in which a model selects actions or tools within application-defined limits   |
| Attention            | A neural-network operation that weights information from token positions               |
| Backpressure         | Slowing or rejecting incoming work when downstream capacity is exhausted               |
| Chunk / passage      | A document segment retrieved as one unit                                               |
| Context window       | The model's bounded token capacity; input/output accounting depends on the model       |
| Cosine similarity    | Dot product divided by vector lengths; similarity, not confidence                      |
| Embedding            | A numerical representation; learned text embeddings come from a trained model          |
| Evaluation / eval    | A repeatable assessment against specified examples and criteria                        |
| Fine-tuning          | Additional training that changes a pretrained model's parameters                       |
| Grounding            | Supporting an answer with identified evidence                                          |
| Hallucination        | Generated content that is false or unsupported in the task's context                   |
| Hit@k                | Fraction of labeled questions with a relevant source among the first k results         |
| Inference            | Running a trained model to produce outputs                                             |
| LLM                  | Large language model; a model of language trained at scale                             |
| Parameters / weights | Numbers learned during model training                                                  |
| Prompt               | Instructions and context supplied to a model                                           |
| Prompt injection     | Untrusted content attempting to redirect model behavior or trigger actions             |
| RAG                  | Retrieval-augmented generation: retrieve evidence and use it when generating an answer |
| Reranking            | Scoring retrieved candidates again to improve their order                              |
| Structured output    | Output constrained to a shape; facts still need validation                             |
| Token                | A model-specific unit of input or output, often a piece of text                        |
| Tool calling         | A model requesting a function invocation that the application validates and executes   |
| Vector store         | Storage and search for vectors, usually with IDs and metadata                          |
| Workflow             | Application-defined steps, possibly including model calls                              |

These short definitions support the modules; consult their cited sources for
technical depth and the [resource list](../RESOURCES.md) for further study.
