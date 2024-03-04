def call(body) {
    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()
    
pipeline {
    agent any
        environment {
        // Define environment variables
        
        ZAP_PATH = '"C:\\Program Files\\ZAP\\Zed Attack Proxy\\zap-2.14.0.jar"'
        def zapDir = "${WORKSPACE}"
        def OUTPUT_PATH = "${zapDir}\\results.html"
    }

    stages {
       stage('SonarQube analysis') {
           steps {
               sonar()
           }
       }

   //     New stage for SonarQube Quality Gate check
       stage('Quality Gate') {
           steps {
               timeout(time: 1, unit: 'HOURS') { // Adjust the timeout as necessary
                   script {
                       def qg = waitForQualityGate() // This method will return a QualityGate object
                       if (qg.status != 'OK') {
                           error "Pipeline halted due to quality gate failure: ${qg.status}"
                       }
                   }
               }
           }
       }
 stage('Deployed PHP CODE') {
            steps {
                // Copying files to XAMPP's htdocs directory
                bat "xcopy * E:\\xampp-portable-windows-x64-7.2.34-2-VC15\\xampp\\htdocs\\dashboard /Y"
                // Checking Newman version to ensure it's accessible
                bat "\"C:\\Users\\Ankush Jindal\\AppData\\Roaming\\npm\\newman\" -v"
            }
        }
        stage('ZAP Security Test') {
            steps {
                script {
                    // Execute ZAP Scan
            bat "java -jar ${env.ZAP_PATH} -cmd -quickurl http://localhost:9002/dashboard/LMS/views/ -quickprogress -quickout ${env.OUTPUT_PATH} -port 8090"          
                // If no medium vulnerabilities, proceed to archive the results
            archiveArtifacts artifacts: 'results.html', onlyIfSuccessful: true
                }
            }
        }

   stage('Check HIGH Risk Alerts') {
    steps {
            script {
                def selector = ".risk-3"
def expectedValue = "0"

// Read the report HTML file line by line
def lines = readFile('results.html').readLines().join(' ') // Joining lines to simplify regex matching across line breaks

// Regular expression to match the td with class `risk-3` and then find the next td element
// This assumes that the class `risk-3` is directly within the td element as per your JS logic
def regex =/<td class="${selector.substring(1)}">\s*(.*?)\s*<\/td>\s*<td[^>]*>\s*<div>\s*(.*?)\s*<\/div>\s*<\/td>/

// Flag to track if value is found
def found = false

def match = lines =~ regex
if (match) {
    match.each { m ->
        // Extract and compare values
        def extractedValue = m[2].trim() // m[2] because m[0] is the whole match, m[1] is the content of the first td, and m[2] is the second td
        if (extractedValue == expectedValue) {
            found = true
            return // Exit loop on first successful match
        }
    }
}

if (!found) {
    error "Build failed: Expected value '${expectedValue}' not found using selector '${selector}'"
} else {
    echo "HIGH Risk Alerts check passed!"
}
   }
   }
   }

        
    }

    // Post actions
    post {
        always {
            emailext (
               subject: "Pipeline Status - ${JOB_NAME}: ${BUILD_NUMBER}", 
                body: '''<html>
<body>
<p>Build Status: ${BUILD_STATUS}</p>
<p>Build Number: ${BUILD_NUMBER}</p>
<p>Failed Stage: ${FAILED_STAGE}</p>
<p>Check the <a href="${BUILD_URL}">console output</a> for more details.</p>
<p>SonarQube Report: <a href="${SONAR_REPORT_URL}">View SonarQube Report</a></p>
<p>ZAP Security Report: <a href="${ZAP_REPORT_URL}">View ZAP Report</a></p>
<p>ZAP Security Report attached.</p>
</body>
</html>''',
                to: 'ankush.rdev@gmail.com',
                from: 'jenkins@example.com',
                replyTo: 'jenkins@example.com',
                mimeType: 'text/html',
                attachmentsPattern: 'results.html'
                // Ensure attachments: 'results.html' is correctly configured if you intend to attach the report
            )
        }
    }
}
}
