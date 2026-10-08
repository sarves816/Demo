<!DOCTYPE html>
<html>

<head>
    <title>College Notice Board</title>

    <style>
        body {
            text-align: center;
            font-family: Arial, sans-serif;
            background-color: #e8f0fe;
            margin: 0;
        }

        header {
            background-color: #1e3a8a;
            color: white;
            padding: 25px;
        }

        .container {
            width: 80%;
            margin: 30px auto;
        }

        .notice {
            background-color: white;
            margin: 20px;
            padding: 20px;
            border-radius: 10px;
        }

        .date {
            color: gray;
        }

        footer {
            background-color: #1e3a8a;
            color: white;
            padding: 20px;
        }
    </style>
</head>

<body>

    <header>
        <h1>ABC College of Engineering</h1>
        <h2>Department of Computer Science and Engineering</h2>
    </header>

    <div class="container">

        <h2>College Notice Board</h2>

        <div class="notice">
            <h3>Internal Assessment Examination</h3>
            <p class="date">Date: 15 October 2026</p>
            <p>Internal Assessment examinations will begin from 15 October 2026.</p>
        </div>

        <div class="notice">
            <h3>Hackathon Registration</h3>
            <p class="date">Date: 20 October 2026</p>
            <p>Students can register for the upcoming college hackathon.</p>
        </div>

        <div class="notice">
            <h3>Placement Training Program</h3>
            <p class="date">Date: 25 October 2026</p>
            <p>Placement training sessions will be conducted for final-year students.</p>
        </div>

        <h3>Contact Information</h3>

        <p>Email: cse@abccollege.edu</p>
        <p>Phone: +91 9876543210</p>

    </div>

    <footer>
        <p>ABC College of Engineering © 2026</p>
    </footer>

</body>

</html>













FROM nginx:latest

COPY index.html /usr/share/nginx/html/index.html

EXPOSE 80








apiVersion: apps/v1
kind: Deployment
metadata:
  name: college-notice-board
spec:
  replicas: 2
  selector:
    matchLabels:
      app: college-notice-board
  template:
    metadata:
      labels:
        app: college-notice-board
    spec:
      containers:
      - name: college-notice-board
        image: YOUR_DOCKERHUB_USERNAME/college-notice-board:latest
        ports:
        - containerPort: 80

---
apiVersion: v1
kind: Service
metadata:
  name: college-notice-board-service
spec:
  type: NodePort
  selector:
    app: college-notice-board
  ports:
  - port: 80
    targetPort: 80
    nodePort: 30010

