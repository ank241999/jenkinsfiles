pipeline {
  agent any

  parameters {
    choice(name: 'CHANGE_AUTH_TOKEN', choices: ['yes', 'no'], description: 'Do you want to change the Auth Token?')
    string(name: 'NEW_AUTH_TOKEN', defaultValue: 'kally', description: 'Enter the new authorization token')
  }

  stages {
    stage('Initialize') {
      steps {
        script {
          // Check if the user wants to change the auth token
          if (params.CHANGE_AUTH_TOKEN == 'yes') {
            // User opted to change the token, use the new value
            env.AUTH_TOKEN = params.NEW_AUTH_TOKEN
          } else {
            // User opted not to change, use the default value
            env.AUTH_TOKEN = 'kally'
          }
        }
      }
    }
    stage('Deploy PHP CODE') {
      steps {
        // Copying files to XAMPP's htdocs directory
        bat "xcopy * E:\\xampp-portable-windows-x64-7.2.34-2-VC15\\xampp\\htdocs\\dashboard /Y"
        // Checking Newman version to ensure it's accessible
        bat "\"C:\\Users\\Ankush Jindal\\AppData\\Roaming\\npm\\newman\" -v"
      }
    }
    stage('Run postman collection') {
      steps {
        script {
          // If you want to run Newman, uncomment the following line and ensure the path and file name are correct
          // bat "\"C:\\Users\\Ankush Jindal\\AppData\\Roaming\\npm\\newman\" run E:\\phpapi.postman_collection.json"

          // Use the AUTH_TOKEN environment variable in the curl command
          bat "curl -X GET http://localhost:9002/dashboard/index.php/users -H \"Authorization: Bearer ${env.AUTH_TOKEN}\""
        }
      }
    }
  }
}
