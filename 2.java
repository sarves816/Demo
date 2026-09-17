print("Frontend checks started")

for i in range(1, 4):
    print(f"Frontend test {i} passed")

print("Frontend checks completed")




print("Backend checks started")

for i in range(1, 4):
    print(f"Backend test {i} passed")

print("Backend checks completed")




pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/YOUR_USERNAME/AST07-Project3.git'
            }
        }

        stage('Parallel Tests') {
            parallel {
                stage('Frontend Tests') {
                    steps {
                        bat 'python frontend_check.py'
                    }
                }

                stage('Backend Tests') {
                    steps {
                        bat 'python backend_check.py'
                    }
                }
            }
        }
    }
}
