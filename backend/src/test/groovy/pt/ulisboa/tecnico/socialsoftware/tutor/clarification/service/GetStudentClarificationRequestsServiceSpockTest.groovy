package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class getStudentClarificationRequestsServiceSpockTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    QuizRepository quizRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    QuizQuestionRepository quizQuestionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    QuizAnswerRepository quizAnswerRepository

    @Autowired
    ClarificationRequestRepository clarificationRequestRepository

    @Autowired
    ClarificationRequestAnswerRepository clarificationRequestAnswerRepository;

    @Autowired
    ClarificationService clarificationService

    Course course
    CourseExecution courseExecution
    Question question
    Quiz quiz
    QuizQuestion quizQuestion
    QuizAnswer quizAnswer
    User student
    User teacher
    ClarificationRequest clarificationRequest
    int studentId
    int teacherId
    int reqId

    def setup() {
        course = createCourse(COURSE_NAME)
        courseExecution = createCourseExecution(course, ACRONYM, ACADEMIC_TERM)
        quiz = createQuiz(1, courseExecution, Quiz.QuizType.GENERATED)
        question = createQuestion(1, course)
        quizQuestion = new QuizQuestion(quiz, question, 1)
        student = createStudent(1, "STUDENT", courseExecution)
        teacher = createTeacher(2, "TEACHER", courseExecution)


        def dto = new ClarificationRequestDto()
        dto.setContent("some request")
        clarificationRequest = new ClarificationRequest(student, question, dto)
        student.addClarificationRequest(clarificationRequest)

        courseRepository.save(course)
        courseExecutionRepository.save(courseExecution)
        quizRepository.save(quiz)
        questionRepository.save(question)
        quizQuestionRepository.save(quizQuestion)
        userRepository.save(student)
        userRepository.save(teacher)
        quizAnswerRepository.save(quizAnswer)
        clarificationRequestRepository.save(clarificationRequest)

        studentId = student.getId()
        teacherId = teacher.getId()
        reqId = clarificationRequest.getId()
    }

    private User createStudent(int key, String name, CourseExecution courseExecution) {
        def u = new User()
        u.setKey(key)
        u.setName(name)
        u.setUsername(name)
        u.setRole(User.Role.STUDENT)
        u.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(u)
        return u
    }

    private User createTeacher(int key, String name, CourseExecution courseExecution) {
        def u = new User()
        u.setKey(key)
        u.setName(name)
        u.setUsername(name)
        u.setRole(User.Role.TEACHER)
        u.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(u)
        return u
    }

    private Question createQuestion(int key, Course course) {
        def question = new Question()
        question.setKey(key)
        question.setCourse(course)
        course.addQuestion(question)
        return question
    }

    private Quiz createQuiz(int key, CourseExecution courseExecution, Quiz.QuizType type) {
        def quiz = new Quiz()
        quiz.setKey(key)
        quiz.setType(type)
        quiz.setCourseExecution(courseExecution)
        courseExecution.addQuiz(quiz)
        return quiz
    }

    private CourseExecution createCourseExecution(Course course, String acronym, String term) {
        def courseExecution = new CourseExecution()
        courseExecution.setCourse(course)
        courseExecution.setAcronym(acronym)
        courseExecution.setAcademicTerm(term)
        return courseExecution
    }

    private Course createCourse(String name) {
        def course = new Course()
        course.setName(name)
        return course
    }

    def "student submitted 1 clarification request"() {
        when:
        def result = clarificationService.getStudentClarificationRequests(studentId)

        then:
        result != null
        result.size() == 1
        ClarificationRequestDto req = result[0]
        req.owner == studentId
        req.content == "some request"
        req.questionId == question.getId()
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService();
        }
    }
}
