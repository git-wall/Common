// src/data/cheatSheets.js
const cheatSheets = {
  'Docker': [
    {
      key: '--name',
      description: 'Assign a name to the container',
      example: '--name myapp',
      defaultValue: 'mycontainer'
    },
    {
      key: '-p',
      description: 'Publish container port to host',
      example: '-p 8080:80',
      defaultValue: '8080:80'
    },
    {
      key: '-d',
      description: 'Run container in background (detached mode)',
      example: '-d',
      defaultValue: ''
    },
    {
      key: '-v',
      description: 'Mount volume',
      example: '-v /host/path:/container/path',
      defaultValue: '/host/path:/container/path'
    },
    {
      key: '-e',
      description: 'Set environment variable',
      example: '-e NODE_ENV=production',
      defaultValue: 'KEY=value'
    },
    {
      key: '--rm',
      description: 'Remove container when it exits',
      example: '--rm',
      defaultValue: ''
    },
    {
      key: '-it',
      description: 'Interactive mode with pseudo-TTY',
      example: '-it',
      defaultValue: ''
    }
  ],
  'Kubernetes': [
    {
      key: 'get',
      description: 'Display one or many resources',
      example: 'get pods',
      defaultValue: 'pods'
    },
    {
      key: 'apply',
      description: 'Apply configuration to resource',
      example: 'apply -f deployment.yaml',
      defaultValue: '-f config.yaml'
    },
    {
      key: 'delete',
      description: 'Delete resources',
      example: 'delete pod mypod',
      defaultValue: 'pod myresource'
    },
    {
      key: 'describe',
      description: 'Show detailed information about resource',
      example: 'describe pod mypod',
      defaultValue: 'pod myresource'
    },
    {
      key: 'logs',
      description: 'Print logs for a container',
      example: 'logs mypod',
      defaultValue: 'mypod'
    },
    {
      key: 'exec',
      description: 'Execute commands in container',
      example: 'exec -it mypod -- /bin/bash',
      defaultValue: '-it mypod -- /bin/bash'
    },
    {
      key: 'port-forward',
      description: 'Forward local port to pod',
      example: 'port-forward mypod 8080:80',
      defaultValue: 'mypod 8080:80'
    }
  ],
  'Git': [
    {
      key: 'clone',
      description: 'Clone a repository',
      example: 'clone https://github.com/user/repo.git',
      defaultValue: 'https://github.com/user/repo.git'
    },
    {
      key: 'add',
      description: 'Add files to staging',
      example: 'add .',
      defaultValue: '.'
    },
    {
      key: 'commit',
      description: 'Commit changes',
      example: 'commit -m "Initial commit"',
      defaultValue: '-m "Commit message"'
    },
    {
      key: 'push',
      description: 'Push commits to remote',
      example: 'push origin main',
      defaultValue: 'origin main'
    },
    {
      key: 'pull',
      description: 'Pull changes from remote',
      example: 'pull origin main',
      defaultValue: 'origin main'
    },
    {
      key: 'branch',
      description: 'List, create, or delete branches',
      example: 'branch feature-branch',
      defaultValue: 'new-branch'
    },
    {
      key: 'checkout',
      description: 'Switch branches or restore files',
      example: 'checkout main',
      defaultValue: 'branch-name'
    }
  ],
  'AWS CLI': [
    {
      key: 's3',
      description: 'S3 operations',
      example: 's3 ls s3://mybucket',
      defaultValue: 'ls s3://bucket-name'
    },
    {
      key: 'ec2',
      description: 'EC2 operations',
      example: 'ec2 describe-instances',
      defaultValue: 'describe-instances'
    },
    {
      key: 'iam',
      description: 'IAM operations',
      example: 'iam list-users',
      defaultValue: 'list-users'
    },
    {
      key: 'lambda',
      description: 'Lambda operations',
      example: 'lambda list-functions',
      defaultValue: 'list-functions'
    },
    {
      key: '--region',
      description: 'Specify AWS region',
      example: '--region us-east-1',
      defaultValue: 'us-east-1'
    },
    {
      key: '--profile',
      description: 'Use specific AWS profile',
      example: '--profile myprofile',
      defaultValue: 'default'
    }
  ],
  'Terraform': [
    {
      key: 'init',
      description: 'Initialize Terraform working directory',
      example: 'init',
      defaultValue: ''
    },
    {
      key: 'plan',
      description: 'Create execution plan',
      example: 'plan',
      defaultValue: ''
    },
    {
      key: 'apply',
      description: 'Apply changes',
      example: 'apply',
      defaultValue: ''
    },
    {
      key: 'destroy',
      description: 'Destroy infrastructure',
      example: 'destroy',
      defaultValue: ''
    },
    {
      key: '-var',
      description: 'Set variable value',
      example: '-var="region=us-east-1"',
      defaultValue: 'key=value'
    },
    {
      key: '-var-file',
      description: 'Load variables from file',
      example: '-var-file="terraform.tfvars"',
      defaultValue: 'variables.tfvars'
    }
  ],
  'Ansible': [
    {
      key: '-i',
      description: 'Specify inventory file',
      example: '-i inventory.ini',
      defaultValue: 'inventory.ini'
    },
    {
      key: '-m',
      description: 'Module name to execute',
      example: '-m ping',
      defaultValue: 'ping'
    },
    {
      key: '-a',
      description: 'Module arguments',
      example: '-a "uptime"',
      defaultValue: '"command"'
    },
    {
      key: '--become',
      description: 'Run operations with become',
      example: '--become',
      defaultValue: ''
    },
    {
      key: '--ask-become-pass',
      description: 'Ask for privilege escalation password',
      example: '--ask-become-pass',
      defaultValue: ''
    },
    {
      key: '--limit',
      description: 'Limit to specific hosts',
      example: '--limit webservers',
      defaultValue: 'group-name'
    }
  ],
  'Helm': [
    {
      key: 'install',
      description: 'Install chart',
      example: 'install myrelease ./mychart',
      defaultValue: 'release-name chart-path'
    },
    {
      key: 'upgrade',
      description: 'Upgrade release',
      example: 'upgrade myrelease ./mychart',
      defaultValue: 'release-name chart-path'
    },
    {
      key: 'uninstall',
      description: 'Uninstall release',
      example: 'uninstall myrelease',
      defaultValue: 'release-name'
    },
    {
      key: 'list',
      description: 'List releases',
      example: 'list',
      defaultValue: ''
    },
    {
      key: '--set',
      description: 'Set values',
      example: '--set key=value',
      defaultValue: 'key=value'
    },
    {
      key: '-f',
      description: 'Values file',
      example: '-f values.yaml',
      defaultValue: 'values.yaml'
    }
  ],
  'Prometheus': [
    {
      key: '--config.file',
      description: 'Configuration file path',
      example: '--config.file=prometheus.yml',
      defaultValue: 'prometheus.yml'
    },
    {
      key: '--storage.tsdb.path',
      description: 'Data storage path',
      example: '--storage.tsdb.path=./data',
      defaultValue: './data'
    },
    {
      key: '--web.listen-address',
      description: 'Listen address',
      example: '--web.listen-address=:9090',
      defaultValue: ':9090'
    },
    {
      key: '--web.external-url',
      description: 'External URL',
      example: '--web.external-url=http://localhost:9090',
      defaultValue: 'http://localhost:9090'
    }
  ],
  'Grafana': [
    {
      key: 'admin',
      description: 'Admin commands',
      example: 'admin reset-admin-password',
      defaultValue: 'reset-admin-password'
    },
    {
      key: 'plugins',
      description: 'Plugin management',
      example: 'plugins install grafana-piechart-panel',
      defaultValue: 'install plugin-name'
    },
    {
      key: '--homepath',
      description: 'Path to Grafana installation',
      example: '--homepath=/usr/share/grafana',
      defaultValue: '/usr/share/grafana'
    },
    {
      key: '--config',
      description: 'Configuration file path',
      example: '--config=/etc/grafana/grafana.ini',
      defaultValue: '/etc/grafana/grafana.ini'
    }
  ],
  'Jenkins': [
    {
      key: 'build',
      description: 'Build a job',
      example: 'build my-job',
      defaultValue: 'job-name'
    },
    {
      key: 'list-jobs',
      description: 'List all jobs',
      example: 'list-jobs',
      defaultValue: ''
    },
    {
      key: 'console',
      description: 'Show console output',
      example: 'console my-job',
      defaultValue: 'job-name'
    },
    {
      key: 'create-job',
      description: 'Create a new job',
      example: 'create-job my-job < config.xml',
      defaultValue: 'job-name < config.xml'
    },
    {
      key: 'delete-job',
      description: 'Delete a job',
      example: 'delete-job my-job',
      defaultValue: 'job-name'
    },
    {
      key: 'install-plugin',
      description: 'Install a plugin',
      example: 'install-plugin git',
      defaultValue: 'plugin-name'
    }
  ],
  'GitLab CI': [
    {
      key: 'register',
      description: 'Register a new runner',
      example: 'register',
      defaultValue: ''
    },
    {
      key: 'list',
      description: 'List all configured runners',
      example: 'list',
      defaultValue: ''
    },
    {
      key: 'verify',
      description: 'Verify registered runners',
      example: 'verify',
      defaultValue: ''
    },
    {
      key: 'unregister',
      description: 'Unregister specific runner',
      example: 'unregister --url http://gitlab.com/',
      defaultValue: '--url http://gitlab.com/'
    },
    {
      key: 'run',
      description: 'Run multi runner service',
      example: 'run',
      defaultValue: ''
    },
    {
      key: '--config',
      description: 'Config file path',
      example: '--config /etc/gitlab-runner/config.toml',
      defaultValue: '/etc/gitlab-runner/config.toml'
    }
  ],
  'CircleCI': [
    {
      key: 'config',
      description: 'Validate and process config',
      example: 'config validate',
      defaultValue: 'validate'
    },
    {
      key: 'local',
      description: 'Run jobs locally',
      example: 'local execute --job build',
      defaultValue: 'execute --job job-name'
    },
    {
      key: 'orb',
      description: 'Orb operations',
      example: 'orb list',
      defaultValue: 'list'
    },
    {
      key: 'project',
      description: 'Project operations',
      example: 'project list',
      defaultValue: 'list'
    },
    {
      key: 'setup',
      description: 'Setup CLI',
      example: 'setup',
      defaultValue: ''
    },
    {
      key: '--token',
      description: 'CircleCI API token',
      example: '--token $CIRCLECI_TOKEN',
      defaultValue: '$CIRCLECI_TOKEN'
    }
  ],
  'GitHub Actions': [
    {
      key: 'workflow',
      description: 'Workflow operations',
      example: 'workflow list',
      defaultValue: 'list'
    },
    {
      key: 'run',
      description: 'Run operations',
      example: 'run list',
      defaultValue: 'list'
    },
    {
      key: 'auth',
      description: 'Authentication',
      example: 'auth login',
      defaultValue: 'login'
    },
    {
      key: 'repo',
      description: 'Repository operations',
      example: 'repo create my-repo',
      defaultValue: 'create repo-name'
    },
    {
      key: 'pr',
      description: 'Pull request operations',
      example: 'pr create',
      defaultValue: 'create'
    },
    {
      key: 'issue',
      description: 'Issue operations',
      example: 'issue list',
      defaultValue: 'list'
    }
  ],
  'Azure CLI': [
    {
      key: 'login',
      description: 'Log in to Azure',
      example: 'login',
      defaultValue: ''
    },
    {
      key: 'account',
      description: 'Account operations',
      example: 'account list',
      defaultValue: 'list'
    },
    {
      key: 'vm',
      description: 'Virtual machine operations',
      example: 'vm list',
      defaultValue: 'list'
    },
    {
      key: 'storage',
      description: 'Storage operations',
      example: 'storage account list',
      defaultValue: 'account list'
    },
    {
      key: 'webapp',
      description: 'Web app operations',
      example: 'webapp list',
      defaultValue: 'list'
    },
    {
      key: '--resource-group',
      description: 'Resource group name',
      example: '--resource-group mygroup',
      defaultValue: 'resource-group-name'
    }
  ],
  'GCP CLI': [
    {
      key: 'auth',
      description: 'Authentication',
      example: 'auth login',
      defaultValue: 'login'
    },
    {
      key: 'compute',
      description: 'Compute Engine operations',
      example: 'compute instances list',
      defaultValue: 'instances list'
    },
    {
      key: 'container',
      description: 'Container operations',
      example: 'container clusters list',
      defaultValue: 'clusters list'
    },
    {
      key: 'app',
      description: 'App Engine operations',
      example: 'app deploy',
      defaultValue: 'deploy'
    },
    {
      key: 'config',
      description: 'Configuration management',
      example: 'config set project my-project',
      defaultValue: 'set project project-id'
    },
    {
      key: '--project',
      description: 'Project ID',
      example: '--project my-project',
      defaultValue: 'project-id'
    }
  ],
  'Nginx': [
    {
      key: '-t',
      description: 'Test configuration',
      example: '-t',
      defaultValue: ''
    },
    {
      key: '-s',
      description: 'Send signal to master process',
      example: '-s reload',
      defaultValue: 'reload'
    },
    {
      key: '-c',
      description: 'Configuration file',
      example: '-c /etc/nginx/nginx.conf',
      defaultValue: '/etc/nginx/nginx.conf'
    },
    {
      key: '-g',
      description: 'Global directives',
      example: '-g "daemon off;"',
      defaultValue: '"daemon off;"'
    },
    {
      key: '-p',
      description: 'Prefix path',
      example: '-p /usr/local/nginx/',
      defaultValue: '/usr/local/nginx/'
    },
    {
      key: '-v',
      description: 'Show version',
      example: '-v',
      defaultValue: ''
    }
  ],
  'Apache': [
    {
      key: 'start',
      description: 'Start Apache',
      example: 'start',
      defaultValue: ''
    },
    {
      key: 'stop',
      description: 'Stop Apache',
      example: 'stop',
      defaultValue: ''
    },
    {
      key: 'restart',
      description: 'Restart Apache',
      example: 'restart',
      defaultValue: ''
    },
    {
      key: 'reload',
      description: 'Reload configuration',
      example: 'reload',
      defaultValue: ''
    },
    {
      key: 'configtest',
      description: 'Test configuration',
      example: 'configtest',
      defaultValue: ''
    },
    {
      key: 'status',
      description: 'Show status',
      example: 'status',
      defaultValue: ''
    }
  ],
  'Redis': [
    {
      key: '-h',
      description: 'Server hostname',
      example: '-h localhost',
      defaultValue: 'localhost'
    },
    {
      key: '-p',
      description: 'Server port',
      example: '-p 6379',
      defaultValue: '6379'
    },
    {
      key: '-a',
      description: 'Authentication password',
      example: '-a mypassword',
      defaultValue: 'password'
    },
    {
      key: '-n',
      description: 'Database number',
      example: '-n 0',
      defaultValue: '0'
    },
    {
      key: '--eval',
      description: 'Execute Lua script',
      example: '--eval "return redis.call(\'ping\')" 0',
      defaultValue: '"script" keys'
    },
    {
      key: '--latency',
      description: 'Enter latency monitoring mode',
      example: '--latency',
      defaultValue: ''
    }
  ],
  'MongoDB': [
    {
      key: '--host',
      description: 'MongoDB host',
      example: '--host localhost:27017',
      defaultValue: 'localhost:27017'
    },
    {
      key: '--port',
      description: 'MongoDB port',
      example: '--port 27017',
      defaultValue: '27017'
    },
    {
      key: '--username',
      description: 'Username for authentication',
      example: '--username myuser',
      defaultValue: 'username'
    },
    {
      key: '--password',
      description: 'Password for authentication',
      example: '--password mypassword',
      defaultValue: 'password'
    },
    {
      key: '--authenticationDatabase',
      description: 'Authentication database',
      example: '--authenticationDatabase admin',
      defaultValue: 'admin'
    },
    {
      key: '--eval',
      description: 'Execute JavaScript',
      example: '--eval "db.users.find()"',
      defaultValue: '"javascript_code"'
    }
  ],
  'MySQL': [
    {
      key: '-h',
      description: 'MySQL host',
      example: '-h localhost',
      defaultValue: 'localhost'
    },
    {
      key: '-P',
      description: 'MySQL port',
      example: '-P 3306',
      defaultValue: '3306'
    },
    {
      key: '-u',
      description: 'Username',
      example: '-u root',
      defaultValue: 'username'
    },
    {
      key: '-p',
      description: 'Password (will prompt)',
      example: '-p',
      defaultValue: ''
    },
    {
      key: '-D',
      description: 'Database name',
      example: '-D mydb',
      defaultValue: 'database_name'
    },
    {
      key: '-e',
      description: 'Execute SQL command',
      example: '-e "SHOW DATABASES;"',
      defaultValue: '"SQL_COMMAND"'
    }
  ]
};

export const getCheatSheet = (technologyName) => {
  return cheatSheets[technologyName] || [];
};

export const getAllCheatSheets = () => {
  return cheatSheets;
};

export const addCheatSheet = (technologyName, cheatSheetItems) => {
  cheatSheets[technologyName] = cheatSheetItems;
};

export default cheatSheets;