pipeline {
    agent any

    stages {
        stage('Deployed PHP CODE') {
            steps {
                // Copying files to XAMPP's htdocs directory
                bat "xcopy * E:\\xampp-portable-windows-x64-7.2.34-2-VC15\\xampp\\htdocs\\dashboard /Y"
                // Checking Newman version to ensure it's accessible
                bat "\"C:\\Users\\Ankush Jindal\\AppData\\Roaming\\npm\\newman\" -v"
            }
        }

        // SonarQube analysis stage for PHP
        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv('sonar') { // Use the SonarQube environment name configured in Jenkins
                    // Use bat for Windows-based systems
                    bat 'sonar-scanner'
                }
            }
        }

        // Other stages can follow here
    }

    // Post actions
    post {
        always {
            emailext (
                subject: "Pipeline Status: ${BUILD_NUMBER}", 
                body: '''<html>
<body>
<p>Build Status: ${BUILD_STATUS}</p>
<p>Build Number: ${BUILD_NUMBER}</p>
<p>Check the <a href="${BUILD_URL}">console output</a> for more details.</p>
</body>
</html>''',
                to: 'ankush.rdev@gmail.com',
                from: 'jenkins@example.com',
                replyTo: 'jenkins@example.com',
                mimeType: 'text/html'
            )
        }
    }
}
