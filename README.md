# scripts-manager
System focused on script execution for several languages.

This sytem allows to register some scripts to be stored for further execution, manually or periodical. Although this system is focused in ETLs, any user who uses it can perform their scripts for any use.

## Sources
List of sources from which I have obtained the knowledge necessary to develop this project:

### Sources for server consensus algorithm
* https://medium.com/@juan.baranowa/algor%C3%ADtmos-de-consenso-raft-y-paxos-b252e51e911a
* https://es.wikipedia.org/wiki/Raft
* https://hmong.es/wiki/Raft_(computer_science)

### Sources for RabbitMQ deployment
* https://github.com/serkodev/rabbitmq-cluster-docker/tree/master - GitHub project with an example of a RabbitMQ cluster for docker-compose deploy

## Requirements

![Bash](https://img.shields.io/badge/Bash-*-8A2BE2)

![Java](https://img.shields.io/badge/Java-17+-red)

![Maven](https://img.shields.io/badge/Maven-3.9+-yellowgreen)

![Quarkus](https://img.shields.io/badge/Quarkus-3+-blue)

![Docker](https://img.shields.io/badge/Docker-24+-0073ec)

\* Bash is needed to make some operations over monorepo, but, in future, will be possible make this operations with Windows' command prompt and Powershell too.

## Repository structure
This repository uses a monorepo structure, this is, inside this repository there are multiple projects that they could be stored in different repositories, but, how there are projects uses for same product or software, they can be placed at same repository.

The folder structure of the project can be shown in this schema:
```bash
.
├── README.md # This README file
├── apps # All executable applications
├── docs # Some documentation additional files
├── hooks # Folder with all git hooks
│   └── pre-commit # Pre-commit git hook to run test before commit
├── infrastructure # Folder with needed infrastructure for local testing
│   ├── docker-compose.yml # Docker-compose file to deploy infrastructure
│   ├── haproxy # Folder to link with Haproxy volume
│   └── rabbitmq # Folder to link with RabbitMQ volume
├── libs # All libraries needed for apps projects
│   └── distributed-utils # Library with some util classes to define distributed-system-based operations
├── project.code-workspace # VSCode workspace file
├── tools # Some script tools needed for VSCode workspace tasks
│   └── install-libs # Task's script to compile and install project's maven libs
└── ui # Folder to store frontend projects
```

## Repository management
To make easy and also to limit user's required dependencies, the monorepo mangement ha been implemented using VSCode's tasks. So, if you want to make any monorepo operation, just make this steps:

1. Open Command Palette:
    * MacOS: ⇧⌘P
    * Linux: Ctrl+Shift+P
    * Windows: Ctrl+Shift+P
2. Search for "Run Task" task and select
3. Search for one of repository's task:
    * **Infrastructure UP**: Executes a "docker-compose up" to deploy infrastructure on local docker
    * **Infrastructure DOWN**: Executes a "docker-compose down" to undeploy previously deployed infrastructure on local docker
    * **Install libs**: Builds and install all projects inside /libs folder in user's local Maven repository

## Git integration
To make easy for developers programming in this monorepo, an automatic task is added to VSCode workspace to set local repository hooks path. With this, when a developer tries to make a commit, first, pre-commit hook checks if the changes are related to any test and, if commit not pass the tests it no applies.