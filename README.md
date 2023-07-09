# scripts-manager
System focused on the execution of scripts of different languages on a distributed system.

This project allows to build a distributed system with the capacity to store and execute scripts in several languages in order to give an answer to needs such as periodic execution of tasks, execution of ETLs, and any need based on the execution of scripts in a high availability system. To ensure high availability, a server consensus algorithm is used, which allows the servers that form part of the system to be managed autonomously at all times; in addition, asynchronous communications are used, based on RabbitMQ, which allows the system to be completely reactive, with almost immediate responses to any external communication.

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
│   └── node # Project to represent a part of the complete solution, the cluster's node
├── docs # Some documentation additional files
├── hooks # Folder with all git hooks
│   └── pre-commit # Pre-commit git hook to run test before commit
├── infrastructure # Folder with needed infrastructure for local testing
│   ├── docker-compose.yml # Docker-compose file to deploy infrastructure
│   ├── haproxy # Folder to link with Haproxy volume
│   └── rabbitmq # Folder to link with RabbitMQ volume
├── libs # All libraries needed for apps projects
│   └── distributed-utils # Library with some util classes to define distributed-system-based operations
│   └── script-runner # Library which contains some elements with the ability to run scripts of a concrete language
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
To make easy for developers programming in this monorepo, an automatic task is added to VSCode workspace to set local repository hooks path. With this, when a developer tries to make a commit, first, pre-commit hook checks if the changes are related to any test and, if commit not pass the tests it no applies. Here are listed all the hooks of the project and a short explanation of each one of them:

### commit-msg
Checks that the commit message written by developer complies with Angular conventional commit structure, defined in https://www.conventionalcommits.org/en/v1.0.0-beta.4/. This hook checks the commit message structure with a regex and, if message is not valid, blocks the commit.

### pre-commit
Checks if the commit applies to any project with tests and, if so, run tests. If tests fails, blocks the commit.

## Projects
As mentioned above, this is a monorepository, so, in the same repository there are multiple interrelated projects. In this section all projects are listed and briefly explained:

### distributed-utils
Located at libs/distributed-utils. Is a Maven project which defines a Java library with some utils to create a cluster node, this is, a server that is part of a group of servers and have the ability of communicate with them and working together. The most important part of this project is the consensus algorithm, because is the logic used to select the leader in a cluster and this is ready to make it asyncrhonously and without the need to know all nodes beforehand. 

### script-runner
Located at libs/script-runner. Is a Maven project which defines a piece of software with the ability to run scripts of several languages. All scripts are processed in a separate process, using the corresponding interpreter, so this project has the requirement of have installed the required interpreter for its correct operation.

### node
Located at apps/node. Is a Maven project which defines a component of complete solution, called node o cluster's node. A node is an element inside a cluster that can make some operations, in this case can run scripts of differente languages, and also can communicate with another nodes using messages through RabbitMQ. One of all nodes in a cluster is called leader, because is the responsible for receive all users requests and select another node, or himself, to process it.