package com.example.Sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class Controller {

     @Autowired
      UserRepository repo;

     @Autowired
     private LeaveRepository leaveRepo;

     @Autowired
     private ComplaintRepository complaintRepo;

     @Autowired
     private NoticeRepository noticeRepository;

    @Autowired
    private RoomRepository roomRepository;

      @PostMapping("/register")
      public User registerUser(@RequestBody User user){
            return repo.save(user);
      }

      @DeleteMapping("/delete/{id}")
    String deleteUser(@PathVariable int id){
          repo.deleteById(id);
          return "User deleted";
      }

      @PutMapping("/update/{id}")
    User updateUser(@PathVariable int id,@RequestBody User newUser){
          User user =repo.findById(id).orElse(null);
          if (user!=null){
              user.setName(newUser.getName());
              user.setRegNo(newUser.getRegNo());
              user.setDept(newUser.getDept());
              user.setYear(newUser.getYear());
              user.setPhone(newUser.getPhone());
              user.setParentPhone(newUser.getParentPhone());
              user.setAddress(newUser.getAddress());
              user.setPassword(newUser.getPassword());
              return repo.save(user);
          }
          return null;
      }

      @GetMapping("/user/{id}")
    User getUser(@PathVariable int id){
          return repo.findById(id).orElse(null);
      }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginData) {

        User user = repo.findByRegNoAndPassword(loginData.getRegNo(), loginData.getPassword());

        if (user != null) {

            return ResponseEntity.ok(user);
        } else {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials");
        }
    }

    @PostMapping("/leave")
    public ResponseEntity<String> applyLeave(@RequestBody Leave leaveRequest) {

        leaveRequest.setStatus("Pending");
        leaveRepo.save(leaveRequest);
        return ResponseEntity.ok("Leave Applied Successfully");
    }

    @GetMapping("/my-leaves/{regNo}")
    public ResponseEntity<List<Leave>> getMyLeaves(@PathVariable String regNo) {
        List<Leave> leaves = leaveRepo.findByRegNo(regNo);
        return ResponseEntity.ok(leaves);
    }


    @PostMapping("/complaint")
    public ResponseEntity<String> registerComplaint(@RequestBody Complaint complaintReq) {
        complaintReq.setStatus("Open"); // Pudhu complaint eppovum "Open" thaan
        complaintRepo.save(complaintReq);
        return ResponseEntity.ok("Complaint Registered");
    }

    @GetMapping("/leaves/all")
    public ResponseEntity<List<Leave>> getAllLeaves() {
        return ResponseEntity.ok(leaveRepo.findAll());
    }

    @PutMapping("/leave/update/{id}")
    public ResponseEntity<String> updateLeaveStatus(@PathVariable int id, @RequestParam String status) {
        Leave leave = leaveRepo.findById(id).orElse(null);
        if (leave != null) {
            leave.setStatus(status);
            leaveRepo.save(leave);   
            return ResponseEntity.ok("Status Updated Successfully");
        }
        return ResponseEntity.status(404).body("Leave not found");
    }


    @GetMapping("/profile/{regNo}")
    public ResponseEntity<?> getUserProfile(@PathVariable String regNo) {
        System.out.println("React is asking for user profile: " + regNo);
        try {
            User user = repo.findByRegNo(regNo);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Server error");
        }
    }
    @GetMapping("/complaints/all")
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        return ResponseEntity.ok(complaintRepo.findAll());
    }

    @PutMapping("/complaint/resolve/{id}")
    public ResponseEntity<String> resolveComplaint(@PathVariable int id) {
        Complaint complaint = complaintRepo.findById(id).orElse(null);
        if (complaint != null) {
            complaint.setStatus("Resolved"); // Status-a Resolved nu maathurom
            complaintRepo.save(complaint);
            return ResponseEntity.ok("Complaint Resolved Successfully");
        }
        return ResponseEntity.status(404).body("Complaint not found");
    }


    @PostMapping("/add-notices")
    public Notice addNotice(@RequestBody Notice notice) {
        return noticeRepository.save(notice);
    }

    @GetMapping("/get-notices")
    public List<Notice> getAllNotices() {
        return noticeRepository.findAll();
    }


    @GetMapping("/rooms/all")
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @PostMapping("/rooms/add")
    public Room addRoom(@RequestBody Room room) {
        return roomRepository.save(room);
    }

    @PutMapping("/rooms/book/{roomNumber}/{regNo}")
    public ResponseEntity<?> bookRoom(@PathVariable String roomNumber, @PathVariable String regNo) {


        User user = repo.findByRegNo(regNo);
        Room room = roomRepository.findAll().stream()
                .filter(r -> r.getRoomNumber().equals(roomNumber))
                .findFirst().orElse(null);


        if (user == null || room == null) {
            return ResponseEntity.badRequest().body("User or Room not found!");
        }
        if (!user.getRoomNumber().equals("Not Assigned")) {
            return ResponseEntity.badRequest().body("You already have a room!");
        }
        if (room.getOccupiedBeds() >= room.getCapacity()) {
            return ResponseEntity.badRequest().body("Room is full!");
        }


        room.setOccupiedBeds(room.getOccupiedBeds() + 1); // Room-la oru aal add aagiyachu
        user.setRoomNumber(room.getRoomNumber()); // User-ku room number kedachiduchi


        roomRepository.save(room);
        repo.save(user);

        return ResponseEntity.ok("Room Booked Successfully!");
    }
}
