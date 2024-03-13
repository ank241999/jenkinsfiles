stage('Cleaning Workspace') {
            steps {
                script {
                    deleteDir() 
                }
            }
        }
