cd %USERPROFILE%\Desktop
mkdir Assessment8
cd Assessment8
mkdir Q1-University
cd Q1-University







  <!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>University Learning Portal</title>
    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f1f5f9;
            color: #1e293b;
        }

        header {
            background: #123b70;
            color: white;
            padding: 25px;
            text-align: center;
        }

        nav {
            background: #0f2850;
            padding: 14px;
            text-align: center;
        }

        nav a {
            color: white;
            text-decoration: none;
            margin: 0 15px;
        }

        main {
            max-width: 1100px;
            margin: auto;
            padding: 25px;
        }

        h2 {
            color: #123b70;
        }

        .grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 20px;
        }

        .card {
            background: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 3px 10px #00000012;
        }

        footer {
            background: #123b70;
            color: white;
            padding: 15px;
            text-align: center;
        }
    </style>
</head>
<body>
    <header>
        <h1>University Learning Management Portal</h1>
        <p>Learn, Explore and Achieve</p>
    </header>

    <nav>
        <a href="#courses">Courses</a>
        <a href="#faculty">Faculty</a>
        <a href="#resources">Resources</a>
        <a href="#announcements">Announcements</a>
        <a href="#exams">Examinations</a>
    </nav>

    <main>
        <section id="courses">
            <h2>Available Courses</h2>
            <div class="grid">
                <div class="card">
                    <h3>Computer Science</h3>
                    <p>Programming, algorithms and software development.</p>
                </div>
                <div class="card">
                    <h3>Artificial Intelligence</h3>
                    <p>Machine learning, deep learning and intelligent systems.</p>
                </div>
                <div class="card">
                    <h3>Database Management</h3>
                    <p>SQL, database design and data administration.</p>
                </div>
            </div>
        </section>

        <section id="faculty">
            <h2>Faculty Profiles</h2>
            <div class="grid">
                <div class="card">
                    <h3>Dr. Arun Kumar</h3>
                    <p>Professor of Computer Science</p>
                </div>
                <div class="card">
                    <h3>Dr. Priya Sharma</h3>
                    <p>Professor of Artificial Intelligence</p>
                </div>
            </div>
        </section>

        <section id="resources">
            <h2>Learning Resources</h2>
            <div class="card">
                <p>Lecture notes, programming tutorials and laboratory materials.</p>
            </div>
        </section>

        <section id="announcements">
            <h2>Assignment Announcements</h2>
            <div class="card">
                <p>Software Development assignment submission is open.</p>
                <p>Submit your work before the announced deadline.</p>
            </div>
        </section>

        <section id="exams">
            <h2>Examination Schedule</h2>
            <div class="card">
                <p>Monday: Computer Science</p>
                <p>Wednesday: Artificial Intelligence</p>
                <p>Friday: Database Management</p>
            </div>
        </section>
    </main>

    <footer>
        <p>University Learning Portal | Version 1.0</p>
    </footer>
</body>
</html>












        FROM nginx:alpine
COPY index.html /usr/share/nginx/html/index.html
EXPOSE 80






apiVersion: apps/v1
kind: Deployment
metadata:
  name: university-portal
spec:
  replicas: 2
  selector:
    matchLabels:
      app: university-portal
  template:
    metadata:
      labels:
        app: university-portal
    spec:
      containers:
        - name: university-portal
          image: YOUR_DOCKERHUB_USERNAME/university-portal:v1
          ports:
            - containerPort: 80
          readinessProbe:
            httpGet:
              path: /
              port: 80
            initialDelaySeconds: 3
            periodSeconds: 5
            failureThreshold: 2
---
apiVersion: v1
kind: Service
metadata:
  name: university-portal-service
spec:
  type: NodePort
  selector:
    app: university-portal
  ports:
    - port: 80
      targetPort: 80
      nodePort: 30081






      docker build -t YOUR_DOCKERHUB_USERNAME/university-portal:v1 .




      docker run -d --name university-test -p 8081:80 YOUR_DOCKERHUB_USERNAME/university-portal:v1





      pipeline {
    agent any

    stages {
        stage('Clone Code') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/sarves816/Assessment-9.git'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat 'docker build -t sarves816/university-portal:v1 .'
            }
        }

        stage('Test Docker Image') {
            steps {
                bat 'docker image inspect sarves816/university-portal:v1'
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    bat '''
                        echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin
                        if errorlevel 1 exit /b 1
                        docker push sarves816/university-portal:v1
                        if errorlevel 1 exit /b 1
                        docker logout
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                bat '''
                    kubectl apply -f deployment.yaml
                    if errorlevel 1 exit /b 1
                    kubectl rollout status deployment/university-portal --timeout=120s
                    if errorlevel 1 exit /b 1
                '''
            }
        }

        stage('Verify Deployment') {
            steps {
                bat '''
                    kubectl get deployments
                    kubectl get pods
                    kubectl get services
                '''
            }
        }
    }

    post {
        success {
            echo 'University Portal deployed successfully!'
        }

        failure {
            echo 'Pipeline failed. Check Console Output.'
            bat 'kubectl rollout undo deployment/university-portal'
        }
    }
      }











          pipeline {
    agent any

    stages {
        stage('Clone Code') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/sarves816/Assessment-9.git'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat 'docker build -t sarves816/university-portal:v1 .'
            }
        }

        stage('Test Docker Image') {
            steps {
                bat 'docker image inspect sarves816/university-portal:v1'
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DH_USER',
                        passwordVariable: 'DH_PASS'
                    )
                ]) {
                    bat '''
                        @echo off
                        echo %DH_PASS% | docker login -u %DH_USER% --password-stdin
                        if errorlevel 1 exit /b 1

                        docker push sarves816/university-portal:v1
                        if errorlevel 1 exit /b 1

                        docker logout
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    try {
                        bat '''
                            kubectl apply -f deployment.yaml
                            if errorlevel 1 exit /b 1

                            kubectl rollout status deployment/university-portal --timeout=120s
                            if errorlevel 1 exit /b 1
                        '''
                    } catch (Exception e) {
                        bat '''
                            kubectl rollout undo deployment/university-portal
                        '''
                        error('Deployment failed. Rollback attempted.')
                    }
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                bat '''
                    kubectl get deployments
                    kubectl get pods
                    kubectl get services
                '''
            }
        }
    }

    post {
        success {
            echo 'University Portal deployed successfully!'
        }

        failure {
            echo 'Pipeline failed. Check Console Output.'
        }
    }
          }
