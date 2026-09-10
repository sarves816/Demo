package com.ast.ambulance;

public class Ambulance {

    public enum Type {
        BASIC,
        ADVANCED_LIFE_SUPPORT,
        ICU
    }

    public enum State {
        AVAILABLE,
        DISPATCHED,
        EN_ROUTE,
        PATIENT_PICKED_UP,
        HOSPITAL_ARRIVED
    }

    private final String id;
    private final String driverName;
    private final String driverPhone;
    private final Type type;
    private State state = State.AVAILABLE;

    public Ambulance(String id, Type type, String driverName, String driverPhone) {
        this.id = id;
        this.type = type;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public State getState() {
        return state;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setState(State state) {
        this.state = state;
    }
}






package com.ast.ambulance;

import java.time.LocalDateTime;

public class EmergencyRequest {

    public enum Priority {
        CRITICAL(4),
        HIGH(3),
        MODERATE(2),
        NORMAL(1);

        private final int rank;

        Priority(int rank) {
            this.rank = rank;
        }

        public int rank() {
            return rank;
        }
    }

    public enum Status {
        WAITING,
        ASSIGNED,
        EN_ROUTE,
        PATIENT_PICKED_UP,
        HOSPITAL_ARRIVED,
        COMPLETED
    }

    private final String patientId;
    private final String emergencyType;
    private final String pickupLocation;
    private final String destinationHospital;
    private final Priority priority;
    private final double distanceKm;
    private final LocalDateTime createdAt;

    private Status status = Status.WAITING;
    private Ambulance ambulance;
    private long etaMinutes;

    public EmergencyRequest(
            String patientId,
            String emergencyType,
            String pickupLocation,
            String destinationHospital,
            Priority priority,
            double distanceKm) {

        this.patientId = patientId;
        this.emergencyType = emergencyType;
        this.pickupLocation = pickupLocation;
        this.destinationHospital = destinationHospital;
        this.priority = priority;
        this.distanceKm = distanceKm;
        this.createdAt = LocalDateTime.now();
    }

    public String getPatientId() {
        return patientId;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public String getDestinationHospital() {
        return destinationHospital;
    }

    public Priority getPriority() {
        return priority;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public Status getStatus() {
        return status;
    }

    public Ambulance getAmbulance() {
        return ambulance;
    }

    public long getEtaMinutes() {
        return etaMinutes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void assign(Ambulance ambulance, long eta) {
        this.ambulance = ambulance;
        this.etaMinutes = eta;
        this.status = Status.ASSIGNED;
        ambulance.setState(Ambulance.State.DISPATCHED);
    }

    public void setStatus(Status status) {

        this.status = status;

        if (ambulance != null) {

            switch (status) {

                case EN_ROUTE:
                    ambulance.setState(Ambulance.State.EN_ROUTE);
                    break;

                case PATIENT_PICKED_UP:
                    ambulance.setState(Ambulance.State.PATIENT_PICKED_UP);
                    break;

                case HOSPITAL_ARRIVED:
                    ambulance.setState(Ambulance.State.HOSPITAL_ARRIVED);
                    break;

                case COMPLETED:
                    ambulance.setState(Ambulance.State.AVAILABLE);
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public String toString() {

        return patientId +
                " | " + priority +
                " | " + emergencyType +
                " | " + status +
                " | ambulance=" +
                (ambulance == null ? "NONE" : ambulance.getId()) +
                " | ETA=" + etaMinutes + " min";
    }
}









package com.ast.ambulance;

public class InvalidRequestException extends Exception {

    public InvalidRequestException(String message) {
        super(message);
    }
}









package com.ast.ambulance;

import java.util.*;

public class DispatchService {

    private final List<Ambulance> ambulances =
            new ArrayList<>();

    private final List<EmergencyRequest> history =
            new ArrayList<>();

    private final PriorityQueue<EmergencyRequest> waiting =
            new PriorityQueue<>(
                    Comparator
                            .comparingInt(
                                    (EmergencyRequest r) ->
                                            r.getPriority().rank())
                            .reversed()
                            .thenComparing(
                                    EmergencyRequest::getCreatedAt)
            );

    public void addAmbulance(Ambulance ambulance) {

        if (ambulance == null) {
            throw new IllegalArgumentException(
                    "Ambulance cannot be null");
        }

        ambulances.add(ambulance);
        dispatchWaiting();
    }

    public EmergencyRequest createRequest(
            String patientId,
            String emergencyType,
            String pickup,
            String hospital,
            EmergencyRequest.Priority priority,
            double distance)
            throws InvalidRequestException {

        if (patientId == null || patientId.isBlank()
                || emergencyType == null
                || emergencyType.isBlank()
                || pickup == null
                || pickup.isBlank()
                || hospital == null
                || hospital.isBlank()
                || priority == null
                || distance <= 0) {

            throw new InvalidRequestException(
                    "Invalid emergency request details");
        }

        EmergencyRequest request =
                new EmergencyRequest(
                        patientId,
                        emergencyType,
                        pickup,
                        hospital,
                        priority,
                        distance);

        history.add(request);
        waiting.offer(request);

        dispatchWaiting();

        return request;
    }

    private void dispatchWaiting() {

        while (!waiting.isEmpty()) {

            EmergencyRequest request =
                    waiting.peek();

            Ambulance ambulance =
                    findBest(request);

            if (ambulance == null) {
                break;
            }

            waiting.poll();

            request.assign(
                    ambulance,
                    estimate(request, ambulance));
        }
    }

    private Ambulance findBest(
            EmergencyRequest request) {

        return ambulances.stream()
                .filter(a ->
                        a.getState() ==
                                Ambulance.State.AVAILABLE)
                .sorted(
                        Comparator
                                .comparingInt(
                                        (Ambulance a) ->
                                                typeScore(a, request))
                                .reversed()
                                .thenComparingDouble(
                                        a -> request.getDistanceKm())
                )
                .findFirst()
                .orElse(null);
    }

    private int typeScore(
            Ambulance ambulance,
            EmergencyRequest request) {

        return switch (request.getPriority()) {

            case CRITICAL ->
                    ambulance.getType() ==
                            Ambulance.Type.ICU ? 3 :
                    ambulance.getType() ==
                            Ambulance.Type.ADVANCED_LIFE_SUPPORT
                            ? 2 : 1;

            case HIGH ->
                    ambulance.getType() ==
                            Ambulance.Type.ADVANCED_LIFE_SUPPORT
                            ? 3 :
                    ambulance.getType() ==
                            Ambulance.Type.ICU ? 2 : 1;

            default -> 1;
        };
    }

    private long estimate(
            EmergencyRequest request,
            Ambulance ambulance) {

        double speed;

        if (ambulance.getType() ==
                Ambulance.Type.ICU) {

            speed = 45;

        } else if (ambulance.getType() ==
                Ambulance.Type.ADVANCED_LIFE_SUPPORT) {

            speed = 50;

        } else {

            speed = 55;
        }

        return Math.max(
                1,
                Math.round(
                        request.getDistanceKm()
                                / speed * 60));
    }

    public void updateStatus(
            String patientId,
            EmergencyRequest.Status status)
            throws InvalidRequestException {

        EmergencyRequest request =
                history.stream()
                        .filter(r ->
                                r.getPatientId()
                                        .equals(patientId))
                        .findFirst()
                        .orElseThrow(() ->
                                new InvalidRequestException(
                                        "Patient/request not found"));

        if (request.getAmbulance() == null) {

            throw new InvalidRequestException(
                    "No ambulance assigned");
        }

        request.setStatus(status);

        dispatchWaiting();
    }

    public List<EmergencyRequest> getHistory() {

        return Collections.unmodifiableList(history);
    }

    public List<Ambulance> getAmbulances() {

        return Collections.unmodifiableList(ambulances);
    }

    public int getWaitingCount() {

        return waiting.size();
    }
}











package com.ast.ambulance;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        DispatchService service =
                new DispatchService();

        service.addAmbulance(
                new Ambulance(
                        "AMB101",
                        Ambulance.Type.BASIC,
                        "Arun",
                        "9000000001"));

        service.addAmbulance(
                new Ambulance(
                        "AMB102",
                        Ambulance.Type.ADVANCED_LIFE_SUPPORT,
                        "Bala",
                        "9000000002"));

        service.addAmbulance(
                new Ambulance(
                        "AMB103",
                        Ambulance.Type.ICU,
                        "Charan",
                        "9000000003"));

        while (true) {

            System.out.println(
                    "\n===== AMBULANCE DISPATCH SYSTEM =====");

            System.out.println("1. Add Emergency");
            System.out.println("2. Update Status");
            System.out.println("3. View History");
            System.out.println("4. View Ambulances");
            System.out.println("5. Exit");

            System.out.print("Enter choice: ");

            String choice = sc.nextLine();

            try {

                if (choice.equals("1")) {

                    System.out.print("Patient ID: ");
                    String patientId = sc.nextLine();

                    System.out.print("Emergency Type: ");
                    String emergencyType = sc.nextLine();

                    System.out.print("Pickup Location: ");
                    String pickup = sc.nextLine();

                    System.out.print("Destination Hospital: ");
                    String hospital = sc.nextLine();

                    System.out.print(
                            "Priority (CRITICAL/HIGH/MODERATE/NORMAL): ");

                    EmergencyRequest.Priority priority =
                            EmergencyRequest.Priority.valueOf(
                                    sc.nextLine().toUpperCase());

                    System.out.print("Distance in km: ");

                    double distance =
                            Double.parseDouble(sc.nextLine());

                    EmergencyRequest request =
                            service.createRequest(
                                    patientId,
                                    emergencyType,
                                    pickup,
                                    hospital,
                                    priority,
                                    distance);

                    System.out.println("\nRequest Created:");
                    System.out.println(request);

                } else if (choice.equals("2")) {

                    System.out.print("Patient ID: ");
                    String patientId = sc.nextLine();

                    System.out.print(
                            "Status (EN_ROUTE/PATIENT_PICKED_UP/" +
                            "HOSPITAL_ARRIVED/COMPLETED): ");

                    EmergencyRequest.Status status =
                            EmergencyRequest.Status.valueOf(
                                    sc.nextLine().toUpperCase());

                    service.updateStatus(
                            patientId,
                            status);

                    System.out.println(
                            "Status updated successfully.");

                } else if (choice.equals("3")) {

                    System.out.println(
                            "\n===== EMERGENCY HISTORY =====");

                    for (EmergencyRequest r :
                            service.getHistory()) {

                        System.out.println(r);
                    }

                    System.out.println(
                            "Waiting requests: " +
                                    service.getWaitingCount());

                } else if (choice.equals("4")) {

                    System.out.println(
                            "\n===== AMBULANCES =====");

                    for (Ambulance a :
                            service.getAmbulances()) {

                        System.out.println(
                                a.getId() +
                                " | " +
                                a.getType() +
                                " | " +
                                a.getState() +
                                " | Driver: " +
                                a.getDriverName() +
                                " | Phone: " +
                                a.getDriverPhone());
                    }

                } else if (choice.equals("5")) {

                    System.out.println("Program terminated.");
                    break;

                } else {

                    System.out.println(
                            "Invalid option.");
                }

            } catch (Exception e) {

                System.out.println(
                        "ERROR: " + e.getMessage());
            }
        }

        sc.close();
    }
}










package com.ast.ambulance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DispatchServiceTest {

    @Test
    void criticalGetsAmbulance()
            throws Exception {

        DispatchService service =
                new DispatchService();

        service.addAmbulance(
                new Ambulance(
                        "A1",
                        Ambulance.Type.ICU,
                        "Driver",
                        "123"));

        EmergencyRequest request =
                service.createRequest(
                        "P1",
                        "Cardiac",
                        "Location A",
                        "Hospital A",
                        EmergencyRequest.Priority.CRITICAL,
                        10);

        assertEquals(
                "A1",
                request.getAmbulance().getId());
    }

    @Test
    void waitingWhenUnavailable()
            throws Exception {

        DispatchService service =
                new DispatchService();

        service.addAmbulance(
                new Ambulance(
                        "A1",
                        Ambulance.Type.BASIC,
                        "Driver",
                        "123"));

        service.createRequest(
                "P1",
                "Emergency",
                "A",
                "H",
                EmergencyRequest.Priority.HIGH,
                5);

        EmergencyRequest request =
                service.createRequest(
                        "P2",
                        "Emergency",
                        "B",
                        "H",
                        EmergencyRequest.Priority.CRITICAL,
                        5);

        assertNull(request.getAmbulance());

        assertEquals(
                1,
                service.getWaitingCount());
    }

    @Test
    void invalidRequest() {

        DispatchService service =
                new DispatchService();

        assertThrows(
                InvalidRequestException.class,
                () -> service.createRequest(
                        "",
                        "Emergency",
                        "A",
                        "H",
                        EmergencyRequest.Priority.NORMAL,
                        5));
    }

    @Test
    void ambulanceReleased()
            throws Exception {

        DispatchService service =
                new DispatchService();

        service.addAmbulance(
                new Ambulance(
                        "A1",
                        Ambulance.Type.ADVANCED_LIFE_SUPPORT,
                        "Driver",
                        "123"));

        service.createRequest(
                "P1",
                "Emergency",
                "A",
                "H",
                EmergencyRequest.Priority.HIGH,
                5);

        service.updateStatus(
                "P1",
                EmergencyRequest.Status.COMPLETED);

        assertEquals(
                Ambulance.State.AVAILABLE,
                service.getAmbulances()
                        .get(0)
                        .getState());
    }
}









<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.ast</groupId>
    <artifactId>ambulance-dispatch-system</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.2</version>
            <scope>test</scope>
        </dependency>

    </dependencies>

    <build>
        <plugins>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>

        </plugins>
    </build>

</project>



