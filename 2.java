with open("report.txt", "w") as f:
    f.write("Application Report\n")
    f.write("Total Users: 120\n")
    f.write("Active Sessions: 45\n")

print("Report generated.")









pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/YOUR_USERNAME/AST07-Project2.git'
            }
        }

        stage('Generate Report') {
            steps {
                bat 'python app.py'
            }
        }

        stage('Archive Report') {
            steps {
                archiveArtifacts artifacts: 'report.txt', fingerprint: true
            }
        }
    }
}
