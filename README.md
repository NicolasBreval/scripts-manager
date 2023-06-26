# scripts-manager
System focused on script execution for several languages.

This sytem allows to register some scripts to be stored for further execution, manually or periodical. Although this system is focused in ETLs, any user who uses it can perform their scripts for any use.

## Requirements

![Bash](https://img.shields.io/badge/Bash-*-8A2BE2)

![Java](https://img.shields.io/badge/Java-17+-red)

![Maven](https://img.shields.io/badge/Maven-3.9+-yellowgreen)

![Quarkus](https://img.shields.io/badge/Quarkus-3+-blue)

\* Bash is needed to make some operations over monorepo, but, in future, will be possible make this operations with Windows' command prompt and Powershell too.

## Project structure

### VSCode
This project is configured to be used in VSCode IDE, so you can see a file named *project.code-workspace* where are located all VSCode workspace properties.

### Git
This repository is stored in GitHub, so git client is used to commit and push your changes to repository. In order to increase the code security, project allows you to configure some hooks to run before some git operations, like push. To apply this hooks, you must to run this command:

```bash
./project.sh -r install-hooks
```

This command take all scripts inside hooks folder and copy them to .git/hooks folder; this action automatically applies this hooks to you local git.

### Tools
To manage some elements of project there are some scripts, which can be executed from *project.sh* script, located at root of this repository. In *.tools* folder are located all scripts with all operations that project can perform over itself; to run any script over repository, you can call *project.sh* with *-r* option and a name as value, this name must be the same as script you want to execute from *.tools* folder. Currently, this is the list of tools you can perform in repository:

#### infra-up
Used to deploy a docker-compose file in Docker to start all infrastructure dependencies. To perform operation you must to run:

```bash
./project.sh -r infra-up
```

#### infra-down
Undeploy the previously deployed infrastructure with *infra-up* tool. To perform operation you must to run:

```bash
./project.sh -r infra-down
```

#### install-hooks
Applies customized hooks to local git repository. This tool it's only needed first time, because only copies hooks files from root folder to git hooks folder and, since that time, hooks are applied in your repository. To perform operation you must to run:

```bash
./project.sh -r install-hooks
```