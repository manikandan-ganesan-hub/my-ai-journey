# Roadmap

[Home](README.md)

This is a learning repository maintained one useful slice at a time. Dates are
not delivery promises. A topic becomes available only when it contains a useful
explanation; a working example also needs setup, tests, and recorded verification.

## Available now

- [LLM components](learning/llm-components.md): model behavior and application boundaries.
- [Retrieval](learning/retrieval.md): lexical vectors, learned embeddings, and RAG.
- [Java baseline](projects/runbook-assistant/README.md): local retrieval and excerpts.
- [Service design](architecture/document-assistant.md): proposed backend architecture.
- [Glossary](learning/glossary.md), [resources](RESOURCES.md), and CI configuration.

## Next experiments — planned

| Slice                        | Completion criteria                                                                                                                            |
| ---------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| Spring AI generation adapter | Pin compatible versions; run a local model; test timeouts, empty evidence, malformed output, and citations; record model and prompt versions   |
| Retrieval evaluation lab     | Compare lexical and learned embeddings on answerable, paraphrased, misleading, and unanswerable queries; separate tuning and held-out sets     |
| Python comparison            | Reproduce the Java fixture contract and explain differences without duplicating the whole course                                               |
| Structured outputs and tools | Validate schema and business rules; use a fake read-only tool; test denied actions and injection attempts                                      |
| Deployment exercise          | Measure latency distribution, saturation, memory, and per-request usage on stated hardware; document costs without promising universal numbers |

## Wider learning path — planned

| Area                          | Intended outcome                                                                        |
| ----------------------------- | --------------------------------------------------------------------------------------- |
| AI and ML foundations         | Compare rules, supervised learning, clustering, and reinforcement learning              |
| Intuitive mathematics         | Work through vectors, probability, dot products, gradients, and uncertainty             |
| Python essentials             | Read data, use environments, test functions, and understand arrays                      |
| ML workflow                   | Avoid leakage; split data; establish a baseline; explain precision, recall, and F1      |
| Neural networks and attention | Trace a small forward pass and explain what attention does                              |
| Prompt engineering            | Version task instructions and examples; compare changes with evaluation cases           |
| Agents and workflows          | Compare fixed steps with model-selected tools, bounded loops, and human approval        |
| Security and responsible AI   | Test prompt injection, access isolation, privacy, harmful outputs, and provenance       |
| Inference and system design   | Compare hosted and local models, caching, batching, observability, cost, and resilience |

The original outline also mentioned image classification, GANs, fine-tuning,
no-code tools, and broad productivity tooling. These are deferred: they do not
support the first document-assistant learning path. Revisit only with a concrete
experiment and a maintenance owner. No completed work was removed to make room.
