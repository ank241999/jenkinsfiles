pipeline {
  agent any

  parameters {
    choice(name: 'CHANGE_AUTH_TOKEN', choices: ['yes', 'no'], description: 'Do you want to change the Auth Token?')
    string(name: 'NEW_AUTH_TOKEN', defaultValue: 'kally', description: 'Enter the new authorization token')
    // Example of adding more parameters
    string(name: 'PROJECT_NAME', defaultValue: 'MyProject', description: 'Enter your project name')
    booleanParam(name: 'ENABLE_LOGGING', defaultValue: true, description: 'Enable logging?')
  }

  stages {
    stage('Initialize') {
      steps {
        script {
          // Example of reading and printing parameters
          echo "CHANGE_AUTH_TOKEN: ${params.CHANGE_AUTH_TOKEN}"
          echo "NEW_AUTH_TOKEN: ${params.NEW_AUTH_TOKEN}"
          echo "PROJECT_NAME: ${params.PROJECT_NAME}"
          echo "ENABLE_LOGGING: ${params.ENABLE_LOGGING}"

          // Use the parameters in your logic
          if (params.CHANGE_AUTH_TOKEN == 'yes') {
            env.AUTH_TOKEN = params.NEW_AUTH_TOKEN
          } else {
            env.AUTH_TOKEN = 'kally'
          }

          // Additional logic based on new parameters can be added here
          env.PROJECT_NAME = params.PROJECT_NAME
          env.ENABLE_LOGGING = params.ENABLE_LOGGING ? 'true' : 'false'
        }
      }
    }
    stage('Deploy PHP CODE') {
      steps {
        // Deployment steps
        bat "xcopy * E:\\xampp-portable-windows-x64-7.2.34-2-VC15\\xampp\\htdocs\\${env.PROJECT_NAME} /Y"
        bat "\"C:\\Users\\Ankush Jindal\\AppData\\Roaming\\npm\\newman\" -v"
      }
    }
    stage('Run postman collection') {
      steps {
        script {
          // Conditional execution based on ENABLE_LOGGING
          if (env.ENABLE_LOGGING.toBoolean()) {
            echo "Logging is enabled."
          }

          // Example of using the AUTH_TOKEN in a request
          bat "curl -X GET http://localhost:9002/dashboard/index.php/users -H \"Authorization: Bearer ${env.AUTH_TOKEN}\""
        }
      }
    }
  }
}
