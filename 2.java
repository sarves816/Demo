package com.ast.challan;

public class Vehicle {

    public enum Type {
        TWO_WHEELER,
        CAR,
        BUS,
        TRUCK,
        OTHER
    }

    private final String number;
    private final String owner;
    private final Type type;

    private int violationCount;

    public Vehicle(
            String number,
            String owner,
            Type type) {

        this.number = number;
        this.owner = owner;
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public String getOwner() {
        return owner;
    }

    public Type getType() {
        return type;
    }

    public int getViolationCount() {
        return violationCount;
    }

    public void incrementViolations() {
        violationCount++;
    }

    public String classification() {

        if (violationCount >= 5) {
            return "HIGH_RISK";
        }

        if (violationCount >= 3) {
            return "REPEAT_OFFENDER";
        }

        if (violationCount >= 1) {
            return "VIOLATOR";
        }

        return "CLEAN";
    }
}





package com.ast.challan;

import java.time.LocalDateTime;

public class Challan {

    public enum Violation {
        OVER_SPEEDING,
        SIGNAL_VIOLATION,
        ILLEGAL_PARKING
    }

    public enum PaymentStatus {
        UNPAID,
        PAID
    }

    private final String id;
    private final String vehicleNumber;
    private final Violation violation;
    private final String location;
    private final LocalDateTime timestamp;
    private final double speed;
    private final double permittedSpeed;
    private final double fineAmount;
    private final String eventKey;

    private PaymentStatus paymentStatus =
            PaymentStatus.UNPAID;

    public Challan(
            String id,
            String vehicleNumber,
            Violation violation,
            String location,
            LocalDateTime timestamp,
            double speed,
            double permittedSpeed,
            double fineAmount,
            String eventKey) {

        this.id = id;
        this.vehicleNumber = vehicleNumber;
        this.violation = violation;
        this.location = location;
        this.timestamp = timestamp;
        this.speed = speed;
        this.permittedSpeed = permittedSpeed;
        this.fineAmount = fineAmount;
        this.eventKey = eventKey;
    }

    public String getId() {
        return id;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public Violation getViolation() {
        return violation;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double getSpeed() {
        return speed;
    }

    public double getPermittedSpeed() {
        return permittedSpeed;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void pay() {
        paymentStatus = PaymentStatus.PAID;
    }

    @Override
    public String toString() {

        return id +
                " | Vehicle: " +
                vehicleNumber +
                " | " +
                violation +
                " | Fine: ₹" +
                fineAmount +
                " | " +
                paymentStatus +
                " | " +
                timestamp;
    }
}





package com.ast.challan;

public class InvalidVehicleException
        extends Exception {

    public InvalidVehicleException(
            String message) {

        super(message);
    }
        }







package com.ast.challan;

public class DuplicateChallanException
        extends Exception {

    public DuplicateChallanException(
            String message) {

        super(message);
    }
        }






package com.ast.challan;

import java.time.LocalDateTime;
import java.util.*;

public class TrafficService {

    private final Map<String, Vehicle> vehicles =
            new HashMap<>();

    private final List<Challan> challans =
            new ArrayList<>();

    private final Set<String> events =
            new HashSet<>();

    public void registerVehicle(
            String number,
            String owner,
            Vehicle.Type type)
            throws InvalidVehicleException {

        if (number == null ||
                !number.matches(
                        "[A-Za-z]{2}[- ]?[0-9]{1,2}" +
                        "[- ]?[A-Za-z]{1,3}" +
                        "[- ]?[0-9]{1,4}")
                || owner == null
                || owner.isBlank()
                || type == null) {

            throw new InvalidVehicleException(
                    "Invalid vehicle information");
        }

        if (vehicles.containsKey(number)) {

            throw new InvalidVehicleException(
                    "Vehicle already registered");
        }

        vehicles.put(
                number,
                new Vehicle(
                        number,
                        owner,
                        type));
    }

    public Challan issueChallan(
            String vehicleNumber,
            Challan.Violation violation,
            String location,
            LocalDateTime timestamp,
            double speed,
            double permittedSpeed,
            String eventKey)
            throws Exception {

        Vehicle vehicle =
                vehicles.get(vehicleNumber);

        if (vehicle == null) {

            throw new InvalidVehicleException(
                    "Vehicle is not registered");
        }

        if (violation == null
                || location == null
                || location.isBlank()
                || timestamp == null
                || eventKey == null
                || eventKey.isBlank()
                || speed < 0
                || permittedSpeed <= 0) {

            throw new InvalidVehicleException(
                    "Invalid violation information");
        }

        if (events.contains(eventKey)) {

            throw new DuplicateChallanException(
                    "Duplicate challan for this violation event");
        }

        double fine =
                calculateFine(
                        violation,
                        speed,
                        permittedSpeed,
                        vehicle.getViolationCount());

        vehicle.incrementViolations();

        String challanId =
                "CH-" +
                String.format(
                        "%04d",
                        challans.size() + 1);

        Challan challan =
                new Challan(
                        challanId,
                        vehicleNumber,
                        violation,
                        location,
                        timestamp,
                        speed,
                        permittedSpeed,
                        fine,
                        eventKey);

        challans.add(challan);
        events.add(eventKey);

        return challan;
    }

    private double calculateFine(
            Challan.Violation violation,
            double speed,
            double limit,
            int previousViolations) {

        double baseFine;

        switch (violation) {

            case OVER_SPEEDING:

                baseFine =
                        Math.max(
                                500,
                                (speed - limit) * 50);

                break;

            case SIGNAL_VIOLATION:

                baseFine = 1000;

                break;

            case ILLEGAL_PARKING:

                baseFine = 500;

                break;

            default:

                baseFine = 0;
        }

        double multiplier;

        if (previousViolations >= 3) {

            multiplier = 2.0;

        } else if (previousViolations >= 1) {

            multiplier = 1.5;

        } else {

            multiplier = 1.0;
        }

        return baseFine * multiplier;
    }

    public void payChallan(String id)
            throws InvalidVehicleException {

        Challan challan = find(id);

        if (challan == null) {

            throw new InvalidVehicleException(
                    "Challan not found");
        }

        challan.pay();
    }

    private Challan find(String id) {

        return challans.stream()
                .filter(c ->
                        c.getId()
                                .equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public double outstanding(
            String vehicleNumber) {

        return challans.stream()
                .filter(c ->
                        c.getVehicleNumber()
                                .equals(vehicleNumber)
                        && c.getPaymentStatus()
                                == Challan.PaymentStatus.UNPAID)
                .mapToDouble(
                        Challan::getFineAmount)
                .sum();
    }

    public Vehicle getVehicle(
            String number) {

        return vehicles.get(number);
    }

    public List<Challan> getChallans() {

        return Collections.unmodifiableList(
                challans);
    }
}







package com.ast.challan;

import java.time.LocalDateTime;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        TrafficService service =
                new TrafficService();

        while (true) {

            System.out.println(
                    "\n===== TRAFFIC E-CHALLAN SYSTEM =====");

            System.out.println("1. Register Vehicle");
            System.out.println("2. Issue Challan");
            System.out.println("3. Pay Challan");
            System.out.println("4. View Challans");
            System.out.println("5. Outstanding Fine");
            System.out.println("6. Vehicle Classification");
            System.out.println("7. Exit");

            System.out.print("Enter choice: ");

            String choice = sc.nextLine();

            try {

                switch (choice) {

                    case "1":

                        System.out.print(
                                "Vehicle Number: ");

                        String number =
                                sc.nextLine()
                                        .toUpperCase();

                        System.out.print(
                                "Owner Name: ");

                        String owner =
                                sc.nextLine();

                        System.out.print(
                                "Vehicle Type " +
                                "(TWO_WHEELER/CAR/BUS/TRUCK/OTHER): ");

                        Vehicle.Type type =
                                Vehicle.Type.valueOf(
                                        sc.nextLine()
                                                .toUpperCase());

                        service.registerVehicle(
                                number,
                                owner,
                                type);

                        System.out.println(
                                "Vehicle registered successfully.");

                        break;

                    case "2":

                        System.out.print(
                                "Vehicle Number: ");

                        number =
                                sc.nextLine()
                                        .toUpperCase();

                        System.out.print(
                                "Violation " +
                                "(OVER_SPEEDING/" +
                                "SIGNAL_VIOLATION/" +
                                "ILLEGAL_PARKING): ");

                        Challan.Violation violation =
                                Challan.Violation.valueOf(
                                        sc.nextLine()
                                                .toUpperCase());

                        System.out.print(
                                "Location: ");

                        String location =
                                sc.nextLine();

                        System.out.print(
                                "Speed: ");

                        double speed =
                                Double.parseDouble(
                                        sc.nextLine());

                        System.out.print(
                                "Permitted Speed: ");

                        double permittedSpeed =
                                Double.parseDouble(
                                        sc.nextLine());

                        System.out.print(
                                "Violation Event ID: ");

                        String eventId =
                                sc.nextLine();

                        Challan challan =
                                service.issueChallan(
                                        number,
                                        violation,
                                        location,
                                        LocalDateTime.now(),
                                        speed,
                                        permittedSpeed,
                                        eventId);

                        System.out.println(
                                "\nChallan Generated:");

                        System.out.println(challan);

                        break;

                    case "3":

                        System.out.print(
                                "Challan ID: ");

                        String challanId =
                                sc.nextLine();

                        service.payChallan(
                                challanId);

                        System.out.println(
                                "Payment successful.");

                        break;

                    case "4":

                        System.out.println(
                                "\n===== CHALLAN HISTORY =====");

                        for (Challan c :
                                service.getChallans()) {

                            System.out.println(c);
                        }

                        break;

                    case "5":

                        System.out.print(
                                "Vehicle Number: ");

                        number =
                                sc.nextLine()
                                        .toUpperCase();

                        System.out.println(
                                "Outstanding Fine: ₹" +
                                service.outstanding(number));

                        break;

                    case "6":

                        System.out.print(
                                "Vehicle Number: ");

                        number =
                                sc.nextLine()
                                        .toUpperCase();

                        Vehicle vehicle =
                                service.getVehicle(number);

                        if (vehicle == null) {

                            throw new InvalidVehicleException(
                                    "Vehicle not found");
                        }

                        System.out.println(
                                "Classification: " +
                                vehicle.classification());

                        break;

                    case "7":

                        System.out.println(
                                "Program terminated.");

                        sc.close();

                        return;

                    default:

                        System.out.println(
                                "Invalid option.");
                }

            } catch (Exception e) {

                System.out.println(
                        "ERROR: " + e.getMessage());
            }
        }
    }
}







package com.ast.challan;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TrafficServiceTest {

    @Test
    void registerAndIssue()
            throws Exception {

        TrafficService service =
                new TrafficService();

        service.registerVehicle(
                "TN01AB1234",
                "Ravi",
                Vehicle.Type.CAR);

        Challan challan =
                service.issueChallan(
                        "TN01AB1234",
                        Challan.Violation.OVER_SPEEDING,
                        "Chennai",
                        LocalDateTime.now(),
                        80,
                        60,
                        "E1");

        assertTrue(
                challan.getFineAmount() > 0);
    }

    @Test
    void invalidVehicle() {

        TrafficService service =
                new TrafficService();

        assertThrows(
                InvalidVehicleException.class,
                () ->
                        service.registerVehicle(
                                "BAD",
                                "Ravi",
                                Vehicle.Type.CAR));
    }

    @Test
    void duplicateEvent()
            throws Exception {

        TrafficService service =
                new TrafficService();

        service.registerVehicle(
                "TN01AB1234",
                "Ravi",
                Vehicle.Type.CAR);

        service.issueChallan(
                "TN01AB1234",
                Challan.Violation.ILLEGAL_PARKING,
                "Chennai",
                LocalDateTime.now(),
                0,
                40,
                "E1");

        assertThrows(
                DuplicateChallanException.class,
                () ->
                        service.issueChallan(
                                "TN01AB1234",
                                Challan.Violation.ILLEGAL_PARKING,
                                "Chennai",
                                LocalDateTime.now(),
                                0,
                                40,
                                "E1"));
    }

    @Test
    void paymentAndOutstanding()
            throws Exception {

        TrafficService service =
                new TrafficService();

        service.registerVehicle(
                "TN01AB1234",
                "Ravi",
                Vehicle.Type.CAR);

        Challan challan =
                service.issueChallan(
                        "TN01AB1234",
                        Challan.Violation.SIGNAL_VIOLATION,
                        "Chennai",
                        LocalDateTime.now(),
                        30,
                        40,
                        "E1");

        assertEquals(
                challan.getFineAmount(),
                service.outstanding(
                        "TN01AB1234"));

        service.payChallan(
                challan.getId());

        assertEquals(
                0,
                service.outstanding(
                        "TN01AB1234"));
    }

    @Test
    void repeatedViolationHigherFine()
            throws Exception {

        TrafficService service =
                new TrafficService();

        service.registerVehicle(
                "TN01AB1234",
                "Ravi",
                Vehicle.Type.CAR);

        double firstFine =
                service.issueChallan(
                        "TN01AB1234",
                        Challan.Violation.SIGNAL_VIOLATION,
                        "Chennai",
                        LocalDateTime.now(),
                        30,
                        40,
                        "E1")
                        .getFineAmount();

        double secondFine =
                service.issueChallan(
                        "TN01AB1234",
                        Challan.Violation.SIGNAL_VIOLATION,
                        "Chennai",
                        LocalDateTime.now(),
                        30,
                        40,
                        "E2")
                        .getFineAmount();

        assertTrue(
                secondFine > firstFine);
    }
}



<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.ast</groupId>
    <artifactId>traffic-echallan-system</artifactId>
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
