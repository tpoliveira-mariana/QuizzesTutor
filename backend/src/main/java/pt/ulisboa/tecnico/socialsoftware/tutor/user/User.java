package pt.ulisboa.tecnico.socialsoftware.tutor.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    public enum Role {STUDENT, TEACHER, ADMIN, DEMO_ADMIN}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique=true, nullable = false)
    private Integer key;

    @Enumerated(EnumType.STRING)
    private Role role;
    
    @Column(unique=true)
    private String username;

    private String name;
    private String enrolledCoursesAcronyms;

    private Integer numberOfTeacherQuizzes;
    private Integer numberOfStudentQuizzes;
    private Integer numberOfInClassQuizzes;
    private Integer numberOfTeacherAnswers;
    private Integer numberOfInClassAnswers;
    private Integer numberOfStudentAnswers;
    private Integer numberOfCorrectTeacherAnswers;
    private Integer numberOfCorrectInClassAnswers;
    private Integer numberOfCorrectStudentAnswers;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "last_access")
    private LocalDateTime lastAccess;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval=true)
    private Set<QuizAnswer> quizAnswers = new HashSet<>();

    @ManyToMany
    private Set<CourseExecution> courseExecutions = new HashSet<>();

    @ManyToMany
    private Set<Tournament> participantTournaments;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "creator", fetch = FetchType.LAZY, orphanRemoval=true)
    private Set<Tournament> createdTournaments;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private Set<ClarificationRequest> clarificationRequests = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<StudentQuestion> studentQuestions = new HashSet<>();

    public User() {
    }

    public User(String name, String username, Integer key, User.Role role) {
        this.name = name;
        setUsername(username);
        this.key = key;
        this.role = role;
        this.creationDate = LocalDateTime.now();
        this.numberOfTeacherQuizzes = 0;
        this.numberOfInClassQuizzes = 0;
        this.numberOfStudentQuizzes = 0;
        this.numberOfTeacherAnswers = 0;
        this.numberOfInClassAnswers = 0;
        this.numberOfStudentAnswers = 0;
        this.numberOfCorrectTeacherAnswers = 0;
        this.numberOfCorrectInClassAnswers = 0;
        this.numberOfCorrectStudentAnswers = 0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnrolledCoursesAcronyms() {
        return enrolledCoursesAcronyms;
    }

    public void setEnrolledCoursesAcronyms(String enrolledCoursesAcronyms) {
        this.enrolledCoursesAcronyms = enrolledCoursesAcronyms;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(LocalDateTime lastAccess) {
        this.lastAccess = lastAccess;
    }

    public Set<QuizAnswer> getQuizAnswers() {
        return quizAnswers;
    }

    public Set<CourseExecution> getCourseExecutions() {
        return courseExecutions;
    }

    public void setCourseExecutions(Set<CourseExecution> courseExecutions) {
        this.courseExecutions = courseExecutions;
    }

    public Set<ClarificationRequest> getClarificationRequests() { return clarificationRequests; }

    public void addCreatedTournament(Tournament newCreatedTournament){
        createdTournaments.add(newCreatedTournament);
    }

    public void removeCreatedTournament(Integer createdTournamentId){
        Tournament createdTournament = getCreatedTournament(createdTournamentId);
        createdTournaments.remove(createdTournament);
    }

    public Set<Tournament> getCreatedTournaments(){
        return createdTournaments;
    }

    public Tournament getCreatedTournament(Integer createdTournamentId){
        return createdTournaments.stream().filter(tournament -> tournament.getId() == createdTournamentId).collect(Collectors.toList()).get(0);
    }

    public void addParticipantTournament(Tournament newParticipantTournament){
        participantTournaments.add(newParticipantTournament);
    }

    public void removeParticipantTournament(Integer participantTournamentId){
        Tournament createdTournament = getCreatedTournament(participantTournamentId);
        createdTournaments.remove(participantTournamentId);
    }

    public Set<Tournament> getParticipantTournaments(){
        return participantTournaments;
    }

    public Tournament getParticipantTournament(Integer participantTournamentId){
        return participantTournaments.stream().filter(tournament -> tournament.getId() == participantTournamentId).collect(Collectors.toList()).get(0);
    }

    public Integer getNumberOfTeacherQuizzes() {
        if (this.numberOfTeacherQuizzes == null)
            this.numberOfTeacherQuizzes = (int) getQuizAnswers().stream()
                    .filter(QuizAnswer::isCompleted)
                    .filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(Quiz.QuizType.PROPOSED))
                    .count();

        return numberOfTeacherQuizzes;
    }

    public void setNumberOfTeacherQuizzes(Integer numberOfTeacherQuizzes) {
        this.numberOfTeacherQuizzes = numberOfTeacherQuizzes;
    }

    public Integer getNumberOfStudentQuizzes() {
        if(this.numberOfStudentQuizzes == null)
            this.numberOfStudentQuizzes = (int) getQuizAnswers().stream()
                    .filter(QuizAnswer::isCompleted)
                    .filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(Quiz.QuizType.GENERATED))
                    .count();

        return numberOfStudentQuizzes;
    }

    public void setNumberOfStudentQuizzes(Integer numberOfStudentQuizzes) {
        this.numberOfStudentQuizzes = numberOfStudentQuizzes;
    }

    public Integer getNumberOfInClassQuizzes() {
        if (this.numberOfInClassQuizzes == null)
            this.numberOfInClassQuizzes = (int) getQuizAnswers().stream()
                    .filter(QuizAnswer::isCompleted)
                    .filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(Quiz.QuizType.IN_CLASS))
                    .count();

        return numberOfInClassQuizzes;
    }

    public void setNumberOfInClassQuizzes(Integer numberOfInClassQuizzes) {
        this.numberOfInClassQuizzes = numberOfInClassQuizzes;
    }

    public Integer getNumberOfTeacherAnswers() {
        if (this.numberOfTeacherAnswers == null)
            this.numberOfTeacherAnswers = getQuizAnswers().stream()
                    .filter(QuizAnswer::isCompleted)
                    .filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(Quiz.QuizType.PROPOSED))
                    .mapToInt(quizAnswer -> quizAnswer.getQuiz().getQuizQuestions().size())
                    .sum();

        return numberOfTeacherAnswers;
    }

    public void setNumberOfTeacherAnswers(Integer numberOfTeacherAnswers) {
        this.numberOfTeacherAnswers = numberOfTeacherAnswers;
    }

    public Integer getNumberOfInClassAnswers() {
        if (this.numberOfInClassAnswers == null)
            this.numberOfInClassAnswers = getQuizAnswers().stream()
                    .filter(QuizAnswer::isCompleted)
                    .filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(Quiz.QuizType.IN_CLASS))
                    .mapToInt(quizAnswer -> quizAnswer.getQuiz().getQuizQuestions().size())
                    .sum();
            return numberOfInClassAnswers;
    }

    public void setNumberOfInClassAnswers(Integer numberOfInClassAnswers) {
        this.numberOfInClassAnswers = numberOfInClassAnswers;
    }

    public Integer getNumberOfStudentAnswers() {
        if (this.numberOfStudentAnswers == null) {
            this.numberOfStudentAnswers = getQuizAnswers().stream()
                    .filter(QuizAnswer::isCompleted)
                    .filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(Quiz.QuizType.GENERATED))
                    .mapToInt(quizAnswer -> quizAnswer.getQuiz().getQuizQuestions().size())
                    .sum();
        }

        return numberOfStudentAnswers;
    }

    public void setNumberOfStudentAnswers(Integer numberOfStudentAnswers) {
        this.numberOfStudentAnswers = numberOfStudentAnswers;
    }

    public Integer getNumberOfCorrectTeacherAnswers() {
        if (this.numberOfCorrectTeacherAnswers == null)
            this.numberOfCorrectTeacherAnswers = (int) this.getQuizAnswers().stream()
                    .filter(QuizAnswer::isCompleted)
                    .filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(Quiz.QuizType.PROPOSED))
                    .flatMap(quizAnswer -> quizAnswer.getQuestionAnswers().stream())
                    .filter(questionAnswer -> questionAnswer.getOption() != null &&
                            questionAnswer.getOption().getCorrect())
                    .count();

            return numberOfCorrectTeacherAnswers;
    }

    public void setNumberOfCorrectTeacherAnswers(Integer numberOfCorrectTeacherAnswers) {
        this.numberOfCorrectTeacherAnswers = numberOfCorrectTeacherAnswers;
    }

    public Integer getNumberOfCorrectInClassAnswers() {
        if (this.numberOfCorrectInClassAnswers == null)
            this.numberOfCorrectInClassAnswers = (int) this.getQuizAnswers().stream()
                    .filter(QuizAnswer::isCompleted)
                    .filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(Quiz.QuizType.IN_CLASS))
                    .flatMap(quizAnswer -> quizAnswer.getQuestionAnswers().stream())
                    .filter(questionAnswer -> questionAnswer.getOption() != null &&
                        questionAnswer.getOption().getCorrect())
                    .count();

        return numberOfCorrectInClassAnswers;
    }

    public void setNumberOfCorrectInClassAnswers(Integer numberOfCorrectInClassAnswers) {
        this.numberOfCorrectInClassAnswers = numberOfCorrectInClassAnswers;
    }

    public Integer getNumberOfCorrectStudentAnswers() {
        if (this.numberOfCorrectStudentAnswers == null)
            this.numberOfCorrectStudentAnswers = (int) this.getQuizAnswers().stream()
                    .filter(QuizAnswer::isCompleted)
                    .filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(Quiz.QuizType.GENERATED))
                    .flatMap(quizAnswer -> quizAnswer.getQuestionAnswers().stream())
                    .filter(questionAnswer -> questionAnswer.getOption() != null &&
                        questionAnswer.getOption().getCorrect())
                    .count();

        return numberOfCorrectStudentAnswers;
    }


    public void setNumberOfCorrectStudentAnswers(Integer numberOfCorrectStudentAnswers) {
        this.numberOfCorrectStudentAnswers = numberOfCorrectStudentAnswers;
    }

    public void increaseNumberOfQuizzes(Quiz.QuizType type) {
        switch (type) {
            case PROPOSED:
                this.numberOfTeacherQuizzes = getNumberOfTeacherQuizzes() + 1;
                break;
            case IN_CLASS:
                this.numberOfInClassQuizzes = getNumberOfInClassQuizzes() + 1;
                break;
            case GENERATED:
                this.numberOfStudentQuizzes = getNumberOfStudentQuizzes() + 1;
                break;
            default:
                break;
        }
    }

    public void increaseNumberOfAnswers(Quiz.QuizType type) {
        switch (type) {
            case PROPOSED:
                this.numberOfTeacherAnswers = getNumberOfTeacherAnswers() + 1;
                break;
            case IN_CLASS:
                this.numberOfInClassAnswers = getNumberOfInClassAnswers() + 1;
                break;
            case GENERATED:
                this.numberOfStudentAnswers = getNumberOfStudentAnswers() + 1;
                break;
            default:
                break;
        }
    }

    public void increaseNumberOfCorrectAnswers(Quiz.QuizType type) {
        switch (type) {
            case PROPOSED:
                this.numberOfCorrectTeacherAnswers = getNumberOfCorrectTeacherAnswers() + 1;
                break;
            case IN_CLASS:
                this.numberOfCorrectInClassAnswers = getNumberOfCorrectInClassAnswers() + 1;
                break;
            case GENERATED:
                this.numberOfCorrectStudentAnswers = getNumberOfCorrectStudentAnswers() + 1;
                break;
            default:
                break;
        }
    }

    public void addQuizAnswer(QuizAnswer quizAnswer) {
        this.quizAnswers.add(quizAnswer);
    }

    public void addCourse(CourseExecution course) {
        this.courseExecutions.add(course);
    }

    public void addClarificationRequest(ClarificationRequest clarificationRequest) { this.clarificationRequests.add(clarificationRequest); }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", role=" + role +
                ", id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", courseAcronyms='" + enrolledCoursesAcronyms + '\'' +
                ", numberOfTeacherQuizzes=" + numberOfTeacherQuizzes +
                ", numberOfInClassQuizzes=" + numberOfInClassQuizzes +
                ", numberOfStudentQuizzes=" + numberOfStudentQuizzes +
                ", numberOfTeacherAnswers=" + numberOfTeacherAnswers +
                ", numberOfCorrectTeacherAnswers=" + numberOfCorrectTeacherAnswers +
                ", numberOfInClassAnswers=" + numberOfInClassAnswers +
                ", numberOfCorrectInClassAnswers=" + numberOfCorrectInClassAnswers +
                ", numberOfStudentAnswers=" + numberOfStudentAnswers +
                ", numberOfCorrectStudentAnswers=" + numberOfCorrectStudentAnswers +
                ", creationDate=" + creationDate +
                ", courseExecutions=" + courseExecutions +
                '}';
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> list = new ArrayList<>();

        list.add(new SimpleGrantedAuthority("ROLE_" + role));

        return list;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public List<Question> filterQuestionsByStudentModel(Integer numberOfQuestions, List<Question> availableQuestions) {
        List<Question> studentAnsweredQuestions = getQuizAnswers().stream()
                .flatMap(quizAnswer -> quizAnswer.getQuestionAnswers().stream())
                .filter(questionAnswer -> availableQuestions.contains(questionAnswer.getQuizQuestion().getQuestion()))
                .filter(questionAnswer -> questionAnswer.getTimeTaken() != null && questionAnswer.getTimeTaken() != 0)
                .map(questionAnswer -> questionAnswer.getQuizQuestion().getQuestion())
                .collect(Collectors.toList());

        List<Question> notAnsweredQuestions = availableQuestions.stream()
                .filter(question -> !studentAnsweredQuestions.contains(question))
                .collect(Collectors.toList());

        List<Question> result = new ArrayList<>();

        // add 80% of notanswered questions
        // may add less if not enough notanswered
        int numberOfAddedQuestions = 0;
        while (numberOfAddedQuestions < numberOfQuestions * 0.8
                && notAnsweredQuestions.size() >= numberOfAddedQuestions + 1) {
            result.add(notAnsweredQuestions.get(numberOfAddedQuestions++));
        }

        // add notanswered questions if there is not enough answered questions
        // it is ok because the total id of available questions > numberOfQuestions
        while (studentAnsweredQuestions.size() + numberOfAddedQuestions < numberOfQuestions) {
            result.add(notAnsweredQuestions.get(numberOfAddedQuestions++));
        }

        // add answered questions
        Random rand = new Random(System.currentTimeMillis());
        while (numberOfAddedQuestions < numberOfQuestions) {
            int next = rand.nextInt(studentAnsweredQuestions.size());
            if(!result.contains(studentAnsweredQuestions.get(next))) {
                result.add(studentAnsweredQuestions.get(next));
                numberOfAddedQuestions++;
            }
        }

        return result;
    }
}