package com.nexaworks.service;

import com.nexaworks.entity.Meeting;
import com.nexaworks.entity.Notification;
import com.nexaworks.entity.User;
import com.nexaworks.enums.Role;
import com.nexaworks.repository.MeetingRepository;
import com.nexaworks.repository.NotificationRepository;
import com.nexaworks.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepo;
    private final MeetingRepository meetingRepo;
    private final NotificationRepository notifRepo;
    private final PasswordEncoder encoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepo.count() > 0) {
            log.info("✅ DB already seeded with {} users", userRepo.count());
            return;
        }
        seedAll();
        seedMeetings();
        seedInitialNotifications();
        log.info("🌱 Seeded {} users into database", userRepo.count());
        log.info("══════════════════════════════════════════════");
        log.info("  HR Login:       sunita.rao@nexaworks.in  / HR@123456");
        log.info("  Manager Login:  vikram.mehta@nexaworks.in / Mgr@123456");
        log.info("  Employee Login: aarav.sharma@nexaworks.in / Emp@123456");
        log.info("══════════════════════════════════════════════");
    }

    // ─── Helper to build and save a User ──────────────────────────────────
    private void save(String name, String email, String rawPwd, Role role,
                      String dept, String mgr, String loc, String gender,
                      int age, int exp, long salary, LocalDate joined, String empCode,
                      int engagement, int taskComp, int loginFreq, int risk, double sentiment,
                      boolean onboardDone, int progress,
                      // docs: pan,aadhaar,voter,passport,salarySlip,offer,10th,12th,degree,expLetter,relLetter,photo
                      boolean pan, boolean aadhaar, boolean voter, boolean passport,
                      boolean salarySlip, boolean offer, boolean tenth, boolean twelfth,
                      boolean degree, boolean expLetter, boolean relLetter, boolean photo,
                      // tasks: it,email,buddy,team,hr,policy,project,t1,t2,t3
                      boolean it, boolean emailT, boolean buddy, boolean team,
                      boolean hrO, boolean policy, boolean proj, boolean t1, boolean t2, boolean t3,
                      String feedback) {

        User u = User.builder()
            .name(name).email(email).password(encoder.encode(rawPwd)).role(role)
            .department(dept).managerName(mgr).location(loc).gender(gender)
            .age(age).experienceYears(exp).salary(salary).joiningDate(joined).employeeCode(empCode)
            .engagementScore(engagement).taskCompletion(taskComp).loginFrequency(loginFreq)
            .riskScore(risk).sentimentScore(sentiment).lastFeedback(feedback)
            .onboardingComplete(onboardDone).onboardingProgress(progress)
            .docPan(pan).docAadhaar(aadhaar).docVoterId(voter).docPassport(passport)
            .docSalarySlip(salarySlip).docOfferLetter(offer).docTenthCert(tenth)
            .docTwelfthCert(twelfth).docDegree(degree).docExperienceLetter(expLetter)
            .docRelievingLetter(relLetter).docPhoto(photo)
            .taskItSetup(it).taskEmailSetup(emailT).taskBuddyMeet(buddy).taskTeamIntro(team)
            .taskHrOrientation(hrO).taskPoliciesRead(policy).taskFirstProject(proj)
            .taskTraining1(t1).taskTraining2(t2).taskTraining3(t3)
            .alertSent(false)
            .build();
        userRepo.save(u);
    }

    private void seedAll() {
        // ══ HR USERS (7) ════════════════════════════════════════════════════
        save("Sunita Rao","sunita.rao@nexaworks.in","HR@123456",Role.HR,
             "HR","CEO","Hyderabad","Female",42,18,130000,LocalDate.of(2019,3,15),"NW-HR-001",
             92,97,10,6,0.90,true,100,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "This platform has transformed our HR operations completely.");

        save("Priya Nair","priya.nair@nexaworks.in","HR@123456",Role.HR,
             "HR","Sunita Rao","Kochi","Female",29,5,68000,LocalDate.of(2024,1,20),"NW-HR-002",
             91,95,10,9,0.88,true,100,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Excellent onboarding system. Very well organized.");

        save("Divya Menon","divya.menon@nexaworks.in","HR@123456",Role.HR,
             "HR","Sunita Rao","Thrissur","Female",28,4,64000,LocalDate.of(2024,3,20),"NW-HR-003",
             88,92,9,11,0.81,true,100,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Automated document verification saves us hours every week.");

        save("Preeti Jain","preeti.jain@nexaworks.in","HR@123456",Role.HR,
             "HR","Sunita Rao","Indore","Female",27,3,61000,LocalDate.of(2024,4,25),"NW-HR-004",
             90,94,9,9,0.87,true,100,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "AI-powered insights make proactive HR management possible.");

        save("Suresh Pillai","suresh.pillai@nexaworks.in","HR@123456",Role.HR,
             "HR","Sunita Rao","Kozhikode","Male",31,7,67000,LocalDate.of(2024,6,10),"NW-HR-005",
             87,91,9,11,0.82,true,100,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Automated alerts system is brilliant. No more manual follow-ups.");

        save("Swati Goel","swati.goel@nexaworks.in","HR@123456",Role.HR,
             "HR","Sunita Rao","Faridabad","Female",28,4,63000,LocalDate.of(2024,7,15),"NW-HR-006",
             89,93,9,10,0.85,true,100,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Sentiment analysis feature is particularly valuable for HR.");

        save("Poornima Subramanian","poornima.s@nexaworks.in","HR@123456",Role.HR,
             "HR","Sunita Rao","Salem","Female",29,5,65000,LocalDate.of(2024,8,25),"NW-HR-007",
             91,95,10,8,0.88,true,100,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Best onboarding system I have worked with. Compliance tracking is flawless.");

        // ══ MANAGER USERS (5) ════════════════════════════════════════════════
        save("Vikram Mehta","vikram.mehta@nexaworks.in","Mgr@123456",Role.MANAGER,
             "Engineering","CEO","Bengaluru","Male",38,14,150000,LocalDate.of(2020,6,1),"NW-MGR-001",
             90,98,10,8,0.85,true,100,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Analytics dashboard gives me everything I need to support my team.");

        save("Rahul Kapoor","rahul.kapoor@nexaworks.in","Mgr@123456",Role.MANAGER,
             "Product","CEO","Mumbai","Male",36,12,140000,LocalDate.of(2021,1,10),"NW-MGR-002",
             88,95,10,10,0.83,true,100,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "The risk score insights help me intervene before problems escalate.");

        save("Pooja Gupta","pooja.gupta@nexaworks.in","Mgr@123456",Role.MANAGER,
             "Marketing","CEO","Delhi","Female",35,11,125000,LocalDate.of(2020,9,20),"NW-MGR-003",
             91,96,10,7,0.87,true,100,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Platform reduced onboarding time by 40 percent. Highly recommend.");

        save("Ankit Joshi","ankit.joshi@nexaworks.in","Mgr@123456",Role.MANAGER,
             "Finance","CEO","Ahmedabad","Male",39,15,135000,LocalDate.of(2021,6,15),"NW-MGR-004",
             86,94,9,13,0.80,true,100,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Excellent tool for monitoring financial team onboarding and compliance.");

        save("Meghna Iyer","meghna.iyer@nexaworks.in","Mgr@123456",Role.MANAGER,
             "Operations","CEO","Chennai","Female",37,13,128000,LocalDate.of(2021,3,8),"NW-MGR-005",
             89,95,10,9,0.84,true,100,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Managing team onboarding has never been this streamlined before.");

        // ══ EMPLOYEE USERS – HIGH PERFORMANCE (18) ══════════════════════════
        save("Aarav Sharma","aarav.sharma@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Engineering","Vikram Mehta","Bengaluru","Male",26,2,75000,LocalDate.of(2024,1,15),"NW-EMP-001",
             82,88,9,18,0.75,true,94,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "The onboarding experience was smooth. Team was very welcoming.");

        save("Ananya Iyer","ananya.iyer@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Design","Rahul Kapoor","Chennai","Female",25,2,69000,LocalDate.of(2024,2,20),"NW-EMP-002",
             87,91,9,12,0.82,true,96,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Loved the structured onboarding plan. Buddy system helped me settle in.");

        save("Sneha Reddy","sneha.reddy@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Marketing","Pooja Gupta","Hyderabad","Female",27,3,71000,LocalDate.of(2024,2,10),"NW-EMP-003",
             78,80,8,22,0.65,true,90,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,false,true,true,
             "Good experience overall. Team introduction sessions were very helpful.");

        save("Kavya Krishnan","kavya.krishnan@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Product","Rahul Kapoor","Bengaluru","Female",30,6,95000,LocalDate.of(2024,3,15),"NW-EMP-004",
             89,93,9,11,0.84,true,98,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Exceptional onboarding journey. Product team is highly collaborative.");

        save("Harish Babu","harish.babu@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Engineering","Vikram Mehta","Mysuru","Male",30,6,82000,LocalDate.of(2024,5,20),"NW-EMP-005",
             80,84,8,20,0.72,true,92,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,false,true,
             "Good structured onboarding. Code review culture is healthy.");

        save("Aishwarya Nair","aishwarya.nair@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Product","Rahul Kapoor","Trivandrum","Female",28,4,88000,LocalDate.of(2024,5,5),"NW-EMP-006",
             86,90,9,14,0.80,true,96,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Fantastic onboarding. Product roadmap was clearly explained from day one.");

        save("Shruti Desai","shruti.desai@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Design","Rahul Kapoor","Vadodara","Female",26,2,66000,LocalDate.of(2024,4,15),"NW-EMP-007",
             85,89,8,15,0.79,true,94,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Design system was clearly explained. Figma setup guide was especially helpful.");

        save("Pallavi Rao","pallavi.rao@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Product","Rahul Kapoor","Vizag","Female",29,5,92000,LocalDate.of(2024,6,25),"NW-EMP-008",
             88,92,9,12,0.84,true,98,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Structured 30-60-90 day plan gave me clear goals from day one.");

        save("Geeta Nambiar","geeta.nambiar@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Marketing","Pooja Gupta","Mangaluru","Female",27,3,70000,LocalDate.of(2024,7,5),"NW-EMP-009",
             82,86,8,18,0.74,true,92,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,false,true,true,
             "Creative and engaging onboarding. Brand guidelines session was insightful.");

        save("Rekha Srinivas","rekha.srinivas@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Legal","Ankit Joshi","Bengaluru","Female",35,10,90000,LocalDate.of(2024,6,5),"NW-EMP-010",
             83,87,8,17,0.75,true,94,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Professional and well-organized. Legal compliance modules were very useful.");

        save("Yamini Krishnaswamy","yamini.k@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Product","Rahul Kapoor","Madurai","Female",31,7,96000,LocalDate.of(2024,8,5),"NW-EMP-011",
             87,91,9,12,0.82,true,96,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Superb onboarding. Product thinking workshops in week two were outstanding.");

        save("Bhavna Mehta","bhavna.mehta@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Marketing","Pooja Gupta","Rajkot","Female",27,3,69000,LocalDate.of(2024,9,5),"NW-EMP-012",
             85,89,8,15,0.78,true,94,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,true,true,
             "Great onboarding. Digital marketing tools walkthrough was very practical.");

        save("Rajeev Nayak","rajeev.nayak@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Sales","Pooja Gupta","Bhubaneswar","Male",27,3,60000,LocalDate.of(2024,8,20),"NW-EMP-013",
             79,83,8,21,0.70,true,90,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,false,true,true,true,
             "Sales methodology training was practical. Mentorship program very helpful.");

        save("Nisha Malhotra","nisha.malhotra@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Sales","Pooja Gupta","Amritsar","Female",26,3,59000,LocalDate.of(2024,5,25),"NW-EMP-014",
             77,81,7,24,0.68,true,90,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,false,true,true,
             "Sales training was comprehensive. CRM setup was quick and efficient.");

        save("Aditi Bhatt","aditi.bhatt@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Engineering","Vikram Mehta","Surat","Female",27,3,74000,LocalDate.of(2024,6,15),"NW-EMP-015",
             76,80,7,26,0.67,true,88,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,false,true,true,true,
             "Decent onboarding. Would have liked more pair programming sessions.");

        save("Gaurav Mishra","gaurav.mishra@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Marketing","Pooja Gupta","Allahabad","Male",25,2,65000,LocalDate.of(2024,5,10),"NW-EMP-016",
             72,76,7,28,0.60,true,86,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,false,true,true,true,true,
             "Good experience. Marketing tools and strategies were well explained.");

        save("Lakshmi Venkatesan","lakshmi.v@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Engineering","Vikram Mehta","Coimbatore","Female",27,3,78000,LocalDate.of(2024,4,5),"NW-EMP-017",
             83,87,8,19,0.74,true,92,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,false,true,
             "Good overall. Technical setup was quick. More structured mentorship would help.");

        save("Ritu Agarwal","ritu.agarwal@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Design","Rahul Kapoor","Meerut","Female",26,2,67000,LocalDate.of(2024,7,25),"NW-EMP-018",
             81,85,8,20,0.73,true,90,
             true,true,true,true,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,false,true,true,
             "The design system was clearly explained. Very happy with the onboarding.");

        // ══ EMPLOYEE USERS – MEDIUM RISK (12) ══════════════════════════════
        save("Deepak Kumar","deepak.kumar@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Sales","Pooja Gupta","Delhi","Male",24,1,58000,LocalDate.of(2024,3,1),"NW-EMP-019",
             60,68,6,52,0.45,false,65,
             true,true,false,false,true,true,false,false,false,false,false,false,
             true,true,false,false,true,false,false,false,false,false,
             "Onboarding is okay but training content feels outdated. Needs improvement.");

        save("Manish Chauhan","manish.chauhan@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Engineering","Vikram Mehta","Chandigarh","Male",29,5,76000,LocalDate.of(2024,4,20),"NW-EMP-020",
             65,70,6,43,0.52,false,70,
             true,true,false,false,true,true,false,false,false,false,false,false,
             true,true,true,false,true,false,false,false,false,false,
             "Average experience. Technical documentation could be improved.");

        save("Abhishek Tomar","abhishek.tomar@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Operations","Meghna Iyer","Gurgaon","Male",26,2,57000,LocalDate.of(2024,7,10),"NW-EMP-021",
             68,72,6,38,0.55,false,72,
             true,true,true,false,true,true,false,false,false,false,false,false,
             true,true,true,false,true,false,false,false,false,false,
             "Okay experience. Operations processes could be better documented.");

        save("Akshay Thakur","akshay.thakur@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Engineering","Vikram Mehta","Shimla","Male",28,4,77000,LocalDate.of(2024,9,1),"NW-EMP-022",
             70,74,7,30,0.58,true,82,
             true,true,true,false,true,true,false,false,false,false,false,false,
             true,true,true,true,true,false,false,false,false,false,
             "Satisfactory onboarding. Sprint planning intro could happen earlier.");

        save("Santosh Kumar","santosh.kumar@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Finance","Ankit Joshi","Ranchi","Male",30,6,59000,LocalDate.of(2024,8,1),"NW-EMP-023",
             61,66,5,55,0.48,false,62,
             true,true,false,false,true,true,false,false,false,false,false,false,
             true,true,false,false,true,false,false,false,false,false,
             "Financial tools onboarding was lengthy. Some modules were repetitive.");

        save("Rajesh Pandey","rajesh.pandey@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Finance","Ankit Joshi","Varanasi","Male",34,9,60000,LocalDate.of(2024,5,1),"NW-EMP-024",
             58,63,5,61,0.42,false,58,
             true,true,false,false,false,true,false,false,false,false,false,false,
             true,false,false,false,true,false,false,false,false,false,
             "Financial compliance training is incomplete. Still have pending certifications.");

        save("Vivek Sharma","vivek.sharma@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Finance","Ankit Joshi","Kanpur","Male",28,4,61000,LocalDate.of(2024,6,20),"NW-EMP-025",
             53,58,4,69,0.36,false,52,
             true,false,true,false,false,true,false,false,false,false,false,false,
             true,false,false,false,false,false,false,false,false,false,
             "Too many pending approvals blocking my work. Bureaucracy is slowing everything down.");

        save("Hemlata Chouhan","hemlata.chouhan@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Operations","Meghna Iyer","Bhopal","Female",23,1,54000,LocalDate.of(2024,8,15),"NW-EMP-026",
             74,78,7,26,0.63,true,86,
             true,true,true,false,true,true,false,true,false,false,false,true,
             true,true,false,true,true,false,false,false,true,false,
             "Good first month. Operations manual was comprehensive and team was helpful.");

        save("Meera Pillai","meera.pillai@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Legal","Ankit Joshi","Kochi","Female",33,8,85000,LocalDate.of(2024,3,5),"NW-EMP-027",
             84,86,8,16,0.76,true,94,
             true,true,true,false,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,false,true,
             "Well-organized process. Legal compliance training was thorough and relevant.");

        save("Chandrika Patel","chandrika.patel@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Design","Rahul Kapoor","Ahmedabad","Female",25,2,65000,LocalDate.of(2024,9,15),"NW-EMP-028",
             84,88,8,16,0.76,true,94,
             true,true,true,false,true,true,true,true,true,false,false,true,
             true,true,true,true,true,true,false,true,true,true,
             "Wonderful experience. Design critique sessions helped me align with company standards.");

        save("Lokesh Verma","lokesh.verma@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Finance","Ankit Joshi","Agra","Male",29,5,58000,LocalDate.of(2024,9,10),"NW-EMP-029",
             48,53,4,73,0.33,false,48,
             true,false,true,false,false,true,false,false,false,false,false,false,
             true,false,false,false,false,false,false,false,false,false,
             "Finance system access took 3 weeks to set up properly. This needs to be fixed urgently.");

        save("Ramesh Yadav","ramesh.yadav@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Sales","Pooja Gupta","Patna","Male",25,2,51000,LocalDate.of(2024,7,1),"NW-EMP-030",
             45,50,3,76,0.30,false,45,
             true,true,false,false,false,true,false,false,false,false,false,false,
             true,false,false,false,false,false,false,false,false,false,
             "Sales training was very basic. Expected more product knowledge sessions.");

        // ══ EMPLOYEE USERS – HIGH RISK (13) ════════════════════════════════
        save("Rohit Verma","rohit.verma@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Finance","Ankit Joshi","Mumbai","Male",31,6,62000,LocalDate.of(2024,2,1),"NW-EMP-031",
             55,62,5,67,0.32,false,55,
             true,false,false,false,true,true,false,false,false,false,false,false,
             true,true,false,false,true,false,false,false,false,false,
             "Onboarding process was confusing. Didn't receive clear instructions. Had to follow up multiple times.");

        save("Kiran Patil","kiran.patil@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Engineering","Vikram Mehta","Pune","Male",28,4,80000,LocalDate.of(2024,2,15),"NW-EMP-032",
             40,45,3,81,0.21,false,40,
             false,false,false,false,false,true,false,false,false,false,false,false,
             true,false,false,false,false,false,false,false,false,false,
             "Very disappointed. Was left alone with no guidance. Manager did not check in once in first week.");

        save("Arjun Singh","arjun.singh@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Engineering","Vikram Mehta","Jaipur","Male",26,2,72000,LocalDate.of(2024,3,10),"NW-EMP-033",
             35,40,2,88,0.18,false,35,
             false,true,false,false,false,true,false,false,false,false,false,false,
             false,false,false,false,false,false,false,false,false,false,
             "Terrible experience. Nobody told me what to do. Considering looking for other opportunities.");

        save("Nikhil Agarwal","nikhil.agarwal@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Sales","Pooja Gupta","Bhopal","Male",23,1,52000,LocalDate.of(2024,4,10),"NW-EMP-034",
             30,35,2,91,0.15,false,30,
             false,false,false,false,false,true,false,false,false,false,false,false,
             false,false,false,false,false,false,false,false,false,false,
             "I have no idea what I am supposed to do. Nobody has been assigned to guide me. Very disorganized.");

        save("Piyush Soni","piyush.soni@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Design","Rahul Kapoor","Jodhpur","Male",24,1,63000,LocalDate.of(2024,6,1),"NW-EMP-035",
             38,42,3,85,0.20,false,38,
             true,false,false,false,false,true,false,false,false,false,false,false,
             true,false,false,false,false,false,false,false,false,false,
             "Design tools were never properly set up. Wasted 2 weeks trying to get access. Very frustrating.");

        save("Sanjay Tiwari","sanjay.tiwari@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Operations","Meghna Iyer","Lucknow","Male",32,7,55000,LocalDate.of(2024,4,1),"NW-EMP-036",
             50,55,4,72,0.38,false,50,
             true,true,true,false,false,true,false,false,false,false,false,false,
             true,true,false,false,false,false,false,false,false,false,
             "Onboarding checklist exists but nobody follows up. I feel unsupported in my first month.");

        save("Tanvi Kulkarni","tanvi.kulkarni@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Operations","Meghna Iyer","Nagpur","Female",22,1,53000,LocalDate.of(2024,5,15),"NW-EMP-037",
             44,49,3,78,0.28,false,44,
             false,true,false,false,false,true,false,false,false,false,false,false,
             false,true,false,false,false,false,false,false,false,false,
             "Feeling very lost. Operations team has no clear onboarding process. Need more guidance urgently.");

        save("Mohit Rawat","mohit.rawat@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Engineering","Vikram Mehta","Dehradun","Male",24,1,71000,LocalDate.of(2024,7,20),"NW-EMP-038",
             42,47,3,83,0.25,false,42,
             false,false,true,false,false,true,false,false,false,false,false,false,
             true,false,false,false,false,false,false,false,false,false,
             "I feel completely unprepared for my role. Codebase was never properly explained. No documentation.");

        save("Dinesh Babu","dinesh.babu@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Engineering","Vikram Mehta","Tirunelveli","Male",25,2,69000,LocalDate.of(2024,8,10),"NW-EMP-039",
             36,41,2,87,0.19,false,36,
             false,true,false,false,false,true,false,false,false,false,false,false,
             false,false,false,false,false,false,false,false,false,false,
             "No clear project assigned. Sitting idle for 2 weeks is very demotivating.");

        save("Poonam Sharma","poonam.sharma@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "HR","Sunita Rao","Jaipur","Female",24,1,45000,LocalDate.of(2024,9,1),"NW-EMP-040",
             42,48,3,79,0.26,false,44,
             false,false,false,false,false,true,false,false,false,false,false,false,
             true,false,false,false,false,false,false,false,false,false,
             "No orientation was scheduled. Still waiting for my laptop. Very poor experience so far.");

        save("Ankush Verma","ankush.verma@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Marketing","Pooja Gupta","Lucknow","Male",23,1,48000,LocalDate.of(2024,9,10),"NW-EMP-041",
             38,44,2,84,0.22,false,38,
             false,false,false,false,false,true,false,false,false,false,false,false,
             false,false,false,false,false,false,false,false,false,false,
             "Nobody from the marketing team reached out to me. I joined 10 days ago and still have no assignments.");

        save("Komal Yadav","komal.yadav@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Sales","Pooja Gupta","Indore","Female",22,0,46000,LocalDate.of(2024,9,15),"NW-EMP-042",
             32,37,2,90,0.17,false,32,
             false,false,false,false,false,true,false,false,false,false,false,false,
             false,false,false,false,false,false,false,false,false,false,
             "This is my first job and I am completely lost. There is no one to guide me through the process.");

        save("Suraj Mishra","suraj.mishra@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Engineering","Vikram Mehta","Bhopal","Male",25,2,68000,LocalDate.of(2024,9,20),"NW-EMP-043",
             34,39,2,89,0.20,false,34,
             false,false,false,false,false,true,false,false,false,false,false,false,
             false,false,false,false,false,false,false,false,false,false,
             "Access to developer tools still not provided. Starting to doubt my decision to join.");

        save("Priyanka Jha","priyanka.jha@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Finance","Ankit Joshi","Patna","Female",26,3,56000,LocalDate.of(2024,9,25),"NW-EMP-044",
             40,45,3,82,0.22,false,40,
             false,false,false,false,false,true,false,false,false,false,false,false,
             true,false,false,false,false,false,false,false,false,false,
             "Financial software access not given yet. Cannot start actual work. Very frustrating experience.");

        save("Vishal Tiwari","vishal.tiwari@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Operations","Meghna Iyer","Varanasi","Male",27,4,55000,LocalDate.of(2024,10,1),"NW-EMP-045",
             38,43,2,86,0.21,false,38,
             false,false,false,false,false,true,false,false,false,false,false,false,
             false,false,false,false,false,false,false,false,false,false,
             "Operations manager is never available. No work assigned in first week. Very demotivating.");

        // ══ ADDITIONAL EMPLOYEES to reach 50 total ══════════════════════════
        save("Akanksha Singh","akanksha.singh@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Engineering","Vikram Mehta","Noida","Female",25,2,72000,LocalDate.of(2024,7,15),"NW-EMP-046",
             75,79,7,25,0.65,true,86,
             true,true,true,false,true,true,true,true,true,false,false,true,
             true,true,true,false,true,true,false,true,false,true,
             "Good onboarding experience. Sprint ceremonies were explained well.");

        save("Rohan Desai","rohan.desai@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Marketing","Pooja Gupta","Pune","Male",28,3,63000,LocalDate.of(2024,6,1),"NW-EMP-047",
             73,77,7,27,0.62,true,84,
             true,true,true,false,true,true,true,false,true,false,false,true,
             true,true,true,true,true,true,false,false,true,true,
             "Decent experience. Would have appreciated more one on one sessions in first week.");

        save("Harleen Kaur","harleen.kaur@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Legal","Ankit Joshi","Amritsar","Female",30,6,82000,LocalDate.of(2024,5,15),"NW-EMP-048",
             80,84,8,20,0.71,true,90,
             true,true,true,true,true,true,true,true,true,true,false,true,
             true,true,true,true,true,true,true,false,true,true,
             "Legal team has a great culture. Onboarding was professional and thorough.");

        save("Nitin Gupta","nitin.gupta@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Product","Rahul Kapoor","Delhi","Male",32,8,98000,LocalDate.of(2024,4,10),"NW-EMP-049",
             85,89,8,15,0.78,true,94,
             true,true,true,false,true,true,true,true,true,true,true,true,
             true,true,true,true,true,true,true,true,false,true,
             "Very satisfying onboarding. Product vision and roadmap were clearly communicated.");

        save("Anushka Sharma","anushka.s@nexaworks.in","Emp@123456",Role.EMPLOYEE,
             "Design","Rahul Kapoor","Mumbai","Female",24,1,61000,LocalDate.of(2024,8,25),"NW-EMP-050",
             79,83,7,22,0.70,true,88,
             true,true,true,false,true,true,true,true,true,false,false,true,
             true,true,true,true,true,true,false,true,true,true,
             "Great design team. The onboarding buddy was very helpful in understanding the workflow.");

        seedMeetings();
        seedInitialNotifications();
    }

    private void seedMeetings() {
        LocalDateTime now = LocalDateTime.now();

        userRepo.findByEmail("satyam.singh@nexaworks.in").ifPresent(manager ->
            userRepo.findByEmail("mohit.pandey@nexaworks.in").ifPresent(employee ->
                meetingRepo.save(Meeting.builder()
                    .title("First One-on-One Check-In")
                    .description("Discuss onboarding progress, support needs, and next week goals.")
                    .scheduledAt(now.plusDays(1).withHour(10).withMinute(0))
                    .durationMinutes(30)
                    .meetingType("ONE_ON_ONE")
                    .organizer(manager.getName())
                    .organizerId(manager.getId())
                    .participantId(employee.getId())
                    .meetLink("https://meet.google.com/nexa-onboarding")
                    .status("SCHEDULED")
                    .build())));

        userRepo.findByEmail("vishal.singh@nexaworks.in").ifPresent(manager ->
            userRepo.findByEmail("kriti.verma@nexaworks.in").ifPresent(employee ->
                meetingRepo.save(Meeting.builder()
                    .title("Team Sync and Goal Alignment")
                    .description("Review ongoing tasks and align on key deliverables for the coming week.")
                    .scheduledAt(now.plusDays(2).withHour(14).withMinute(30))
                    .durationMinutes(45)
                    .meetingType("TEAM")
                    .organizer(manager.getName())
                    .organizerId(manager.getId())
                    .participantId(employee.getId())
                    .meetLink("https://meet.google.com/nexa-team-sync")
                    .status("SCHEDULED")
                    .build())));

        userRepo.findByEmail("rahul.kapoor@nexaworks.in").ifPresent(manager ->
            userRepo.findByEmail("akash.sharma@nexaworks.in").ifPresent(employee ->
                meetingRepo.save(Meeting.builder()
                    .title("Design Buddy Review")
                    .description("Meet your design buddy, review initial tasks, and get access details.")
                    .scheduledAt(now.plusDays(3).withHour(16).withMinute(0))
                    .durationMinutes(30)
                    .meetingType("ONE_ON_ONE")
                    .organizer(manager.getName())
                    .organizerId(manager.getId())
                    .participantId(employee.getId())
                    .meetLink("https://meet.google.com/nexa-design-buddy")
                    .status("SCHEDULED")
                    .build())));
    }

    private void seedInitialNotifications() {
        LocalDateTime now = LocalDateTime.now();

        userRepo.findByEmail("mohit.pandey@nexaworks.in").ifPresent(employee ->
            notifRepo.save(Notification.builder()
                .userId(employee.getId())
                .title("Welcome to NexaWorks")
                .message("Your onboarding dashboard is ready. Complete your first task and schedule your check-in with your manager.")
                .type("INFO")
                .severity("LOW")
                .createdAt(now.minusHours(3))
                .build()));

        userRepo.findByEmail("kriti.verma@nexaworks.in").ifPresent(employee ->
            notifRepo.save(Notification.builder()
                .userId(employee.getId())
                .title("Team Meeting Scheduled")
                .message("Your team sync is scheduled for tomorrow at 2:30 PM. Be prepared with updates on your current tasks.")
                .type("ALERT")
                .severity("MEDIUM")
                .createdAt(now.minusHours(2))
                .build()));

        userRepo.findByEmail("akash.sharma@nexaworks.in").ifPresent(employee ->
            notifRepo.save(Notification.builder()
                .userId(employee.getId())
                .title("Task Reminder")
                .message("Upload your identification documents and complete the first training module to keep your onboarding on track.")
                .type("TASK")
                .severity("HIGH")
                .createdAt(now.minusMinutes(45))
                .build()));

        userRepo.findByEmail("vishal.singh@nexaworks.in").ifPresent(manager ->
            notifRepo.save(Notification.builder()
                .userId(manager.getId())
                .title("Manager Alert")
                .message("A new employee has joined your team. Review their onboarding status and welcome them in the next manager sync.")
                .type("INFO")
                .severity("LOW")
                .createdAt(now.minusHours(1))
                .build()));
    }
}
