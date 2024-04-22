def call(body) {
    // evaluate the body block, and collect configuration into the object
    def pipelineParams = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    pipeline {
        agent any

        parameters {
            string(name: 'PROD_URL', defaultValue: 'https://dlmsintegration.marinepals.com/', description: 'Enter the production URL.')
            string(name: 'DLMS_CODE_PATH', defaultValue: 'D:\\_tools\\xampp-portable-windows-x64-7.2.34-2-VC15\\xampp\\htdocs', description: 'Enter the DLMS code path.')
            string(name: 'APP_SOURCE', defaultValue: 'D:\\dmlsapi1', description: 'Enter the source directory for the application.')
            string(name: 'VERSION_CONTROL_PATH', defaultValue: 'D:\\lite_version_control', description: 'Enter the version control path.')
            string(name: 'MY_APP_VERSION', defaultValue: '2.7.3', description: 'Enter the application version.')
            string(name: 'SERVER_TYPE', defaultValue: 'iis', description: 'Enter the server type (e.g., iis, apache).')
        }

        environment {
            ZAP_PATH = '"C:\\Program Files\\ZAP\\Zed Attack Proxy\\zap-2.14.0.jar"'
            def zapDir = "${WORKSPACE}"
            OUTPUT_PATH = "${zapDir}\\results.html" // OUTPUT_PATH is globally available
        }

        stages {
            stage('Initialize') {
                steps {
                    script {
                        echo "Production URL: ${params.PROD_URL}"
                        echo "DLMS Code Path: ${params.DLMS_CODE_PATH}"
                        echo "Application Source: ${params.APP_SOURCE}"
                        echo "Version Control Path: ${params.VERSION_CONTROL_PATH}"
                        echo "MY App Version: ${params.MY_APP_VERSION}"
                        echo "Server Type: ${params.SERVER_TYPE}"
                    }
                }
            }
            stage('SonarQube analysis') {
                steps {
                    sonar()
                }
            }
            stage('Quality Gate') {
                steps {
                    timeout(time: 1, unit: 'HOURS') {
                        script {
                            def qg = waitForQualityGate()
                            if (qg.status != 'OK') {
                                error "Pipeline halted due to quality gate failure: ${qg.status}"
                            }
                        }
                    }
                }
            }
            stage('Deployed Code') {
                steps {
                    bat "xcopy * D:\\_tools\\xampp-portable-windows-x64-7.2.34-2-VC15\\xampp\\htdocs /Y"
                }
            }
            stage('Running the API') {
                steps { 
                    script {
                        // Check if the directory exists and only execute mkdir and xcopy if it does not
                        bat "if not exist \"${params.APP_SOURCE}\\\" (mkdir \"${params.APP_SOURCE}\" && xcopy \"D:\\lite setup\\*\" \"${params.APP_SOURCE}\\\" /E /H /C /I /Y)"
                       // bat "mkdir \"${params.APP_SOURCE}\" && xcopy \"D:\\lite setup\\*\" \"${params.APP_SOURCE}\\\" /E /H /C /I /Y"
                        bat "curl \"http://localhost/dlms_lite_setup/installer/create_dlms_lite_installer.php?prod_url=${params.PROD_URL}&serve_to=3&dlmscode_path=${params.DLMS_CODE_PATH}&MyAppSource=${params.APP_SOURCE}&version_control_path=${params.VERSION_CONTROL_PATH}&MyAppVersion=${params.MY_APP_VERSION}&server_type=${params.SERVER_TYPE}\""
                        def installerPath = "${params.VERSION_CONTROL_PATH}\\${params.MY_APP_VERSION}\\DLMSLite_IIS_installer_${params.MY_APP_VERSION}\\DLMSLite_Installer_v${params.MY_APP_VERSION}.exe"
                        bat "copy ${installerPath} %WORKSPACE%"
                        archiveArtifacts artifacts: "DLMSLite_Installer_v${params.MY_APP_VERSION}.exe", onlyIfSuccessful: true
                    }
                }
            }
        }

        post {
            always {
                emailext(
                    subject: "Pipeline Status - ${JOB_NAME}: ${BUILD_NUMBER}",
                    body: '''<html>
<body>
<p>_______THIS IS TEST EMAIL BY JENKINS PLEASE IGNORE ALL EMAILS________</p>
<p>Build Status: ${BUILD_STATUS}</p>
<p>Build Number: ${BUILD_NUMBER}</p>
<p>Check the <a href="${BUILD_URL}">console output</a> for more details.</p>
<p>ZAP Security Report attached.</p>
</body>
</html>''',
                    to: '${DEFAULT_RECIPIENTS}',
                    from: 'infraitdev@marinepals.com',
                    replyTo: 'noreply@outlokk365.com',
                    mimeType: 'text/html',
                    attachmentsPattern: 'results.html'
                )
            }
        }
    }
}
