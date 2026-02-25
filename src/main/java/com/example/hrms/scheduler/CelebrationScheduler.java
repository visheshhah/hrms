package com.example.hrms.scheduler;

import com.example.hrms.entities.Employee;
import com.example.hrms.entities.Post;
import com.example.hrms.enums.CelebrationType;
import com.example.hrms.repositories.EmployeeRepository;
import com.example.hrms.repositories.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CelebrationScheduler {

    private final EmployeeRepository employeeRepository;
    private final PostRepository postRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    //@Scheduled(cron = "0 */1 * * * ?")
    @Transactional
    public void createDailyCelebrationPosts() {

        LocalDate today = LocalDate.now();

        Employee systemEmployee = getSystemEmployee();

        createBirthdayPosts(today, systemEmployee);
        createAnniversaryPosts(today, systemEmployee);
    }

    // --------------------------------------------------

    private void createBirthdayPosts(LocalDate today, Employee systemEmployee) {

        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        List<Employee> employees =
                employeeRepository.findEmployeesWithBirthday(month, day);

        for (Employee employee : employees) {

            if (isSystemAccount(employee)) continue;

            if (celebrationAlreadyExists(employee, today, CelebrationType.BIRTHDAY))
                continue;

            Post post = buildBirthdayPost(employee, today, systemEmployee);

            postRepository.save(post);
        }
    }

    // --------------------------------------------------

    private void createAnniversaryPosts(LocalDate today, Employee systemEmployee) {

        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        List<Employee> employees =
                employeeRepository.findEmployeesWithAnniversary(month, day);

        for (Employee employee : employees) {

            if (isSystemAccount(employee)) continue;

            int years = Period.between(employee.getJoiningDate(), today).getYears();
            if (years <= 0) continue;

            if (celebrationAlreadyExists(employee, today, CelebrationType.ANNIVERSARY))
                continue;

            Post post = buildAnniversaryPost(employee, today, years, systemEmployee);

            postRepository.save(post);
        }
    }

    // --------------------------------------------------

    private Post buildBirthdayPost(Employee employee,
                                   LocalDate today,
                                   Employee systemEmployee) {

        Post post = new Post();
        post.setTitle("Birthday Celebration 🎂");
        post.setDescription(
                "Today is " + employee.getFirstName() + " " +
                        employee.getLastName() + "'s birthday! 🎉"
        );

        post.setIsSystemGenerated(true);
        post.setCreatedBy(systemEmployee);

        post.setCelebrationEmployee(employee);
        post.setCelebrationDate(today);
        post.setCelebrationType(CelebrationType.BIRTHDAY);

        return post;
    }

    private Post buildAnniversaryPost(Employee employee,
                                      LocalDate today,
                                      int years,
                                      Employee systemEmployee) {

        Post post = new Post();
        post.setTitle("Work Anniversary 🎉");
        post.setDescription(
                employee.getFirstName() +
                        " completes " + years +
                        " year" + (years > 1 ? "s" : "") +
                        " at the organization! 👏"
        );

        post.setIsSystemGenerated(true);
        post.setCreatedBy(systemEmployee);

        post.setCelebrationEmployee(employee);
        post.setCelebrationDate(today);
        post.setCelebrationType(CelebrationType.ANNIVERSARY);

        return post;
    }

    // --------------------------------------------------

    private boolean celebrationAlreadyExists(Employee employee,
                                             LocalDate today,
                                             CelebrationType type) {

        return postRepository
                .existsByCelebrationEmployeeAndCelebrationDateAndCelebrationType(
                        employee,
                        today,
                        type
                );
    }

    private boolean isSystemAccount(Employee employee) {
        return "system@hrms.com".equalsIgnoreCase(employee.getEmail());
    }

    private Employee getSystemEmployee() {
        return employeeRepository.findByEmail("system@hrms.com")
                .orElseThrow(() -> new RuntimeException("System employee not found"));
    }
}