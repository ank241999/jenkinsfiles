pipeline {
    agent any

    stages {
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

                    // Corrected curl command with proper double quotes escaping
                    bat "curl -X GET http://localhost:9002/dashboard/index.php/users -H \"Authorization: Bearer kally\""
                }
            }
        }
    }
}
