package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import spock.lang.Specification
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import spock.lang.Unroll
import java.time.format.DateTimeFormatter

@DataJpaTest
class SubmitClarificationRequestServiceSpockTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"
    static final String CONTENT = "This is a test request."
    static final String USERNAME_ONE = "STUDENT_ONE"
    static final String USERNAME_TWO = "STUDENT_TWO"
    static final String NAME = "NAME"
    static final int INEXISTENT_QUESTION_ID = -1
    static final int KEY_ONE = 1
    static final int KEY_TWO = 2

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
    ClarificationService clarificationService

    def course
    def courseExecution
    def question
    def quiz
    def quizQuestion
    def quizAnswer
    def student
    def clarificationRequestDto
    def formatter
    def studentId
    def questionId

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        course = createCourse(COURSE_NAME)
        courseExecution = createCourseExecution(course, ACRONYM, ACADEMIC_TERM)
        quiz = createQuiz(KEY_ONE, courseExecution, Quiz.QuizType.GENERATED)
        question = createQuestion(KEY_ONE, course)
        quizQuestion = new QuizQuestion(quiz, question, 1)
        student = createStudent(new User(), KEY_ONE, NAME, USERNAME_ONE, courseExecution)
        quizAnswer = new QuizAnswer(student, quiz)

        courseRepository.save(course)
        courseExecutionRepository.save(courseExecution)
        quizRepository.save(quiz)
        questionRepository.save(question)
        quizQuestionRepository.save(quizQuestion)
        userRepository.save(student)
        quizAnswerRepository.save(quizAnswer)


        clarificationRequestDto = new ClarificationRequestDto()
        questionId = question.getId()
        studentId = student.getId()
    }

    private User createStudent(User student, int key, String name, String username, CourseExecution courseExecution) {
        student.setKey(key)
        student.setName(name)
        student.setUsername(username)
        student.setRole(Role.STUDENT)
        student.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(student)
        return student
    }

    private Question createQuestion(int key, Course course) {
        question = new Question()
        question.setKey(key)
        question.setCourse(course)
        course.addQuestion(question)
        return question
    }

    private Quiz createQuiz(int key, CourseExecution courseExecution, Quiz.QuizType type) {
        quiz = new Quiz()
        quiz.setKey(key)
        quiz.setType(type)
        quiz.setCourseExecution(courseExecution)
        courseExecution.addQuiz(quiz)
        return quiz
    }

    private CourseExecution createCourseExecution(Course course, String acronym, String term) {
        courseExecution = new CourseExecution()
        courseExecution.setCourse(course)
        courseExecution.setAcronym(acronym)
        courseExecution.setAcademicTerm(term)
        return courseExecution
    }

    private Course createCourse(String name) {
        course = new Course()
        course.setName(name)
        return course
    }



    def "the question has been answered and submit request"() {
        //the clarification request is created
        when:
        clarificationRequestDto.setContent(CONTENT)
        clarificationRequestDto = clarificationService.submitClarificationRequest(questionId, student, clarificationRequestDto)

        then:"request is created and is in the repository"
        clarificationRequestRepository.count() == 1L
        def result = clarificationRequestRepository.findAll().get(0)
        result.getId() != null
        result.getKey() != null
        result.getOwner().getId() == student.getId()
        result.getQuestion().getId() == question.getId()
        result.getCreationDate() != null
        and: "the clarification request was added to the student"
        def user = userRepository.findAll().get(0)
        user.getClarificationRequests().size() == 1
    }

    def "same student submits 2 requests for the same question"() {
        //throw exception
        given: "a second clarification request dto"
        clarificationRequestDto.setContent(CONTENT)
        def clarificationDto2 = new ClarificationRequestDto()
        clarificationDto2.setContent(CONTENT)

        when:
        clarificationService.submitClarificationRequest(questionId, student, clarificationRequestDto)
        clarificationService.submitClarificationRequest(questionId, student, clarificationDto2)

        then: "only the first one is saved and exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.DUPLICATE_CLARIFICATION_REQUEST
        clarificationRequestRepository.count() == 1L
        and: "the second clarification request wasn't added to the student"
        def user = userRepository.findAll().get(0)
        user.getClarificationRequests().size() == 1
    }


    @Unroll("invalid arguments: #content | #has_answered || #error_message")
    def "invalid arguments"() {
        given:
        def student2 = createStudent(new User(), KEY_TWO, NAME, USERNAME_TWO, courseExecution)
        userRepository.save(student2)

        when:
        User s = changeStudent(has_answered, student2)
        changeQuestionId(is_question)
        clarificationRequestDto.setContent(content)
        clarificationService.submitClarificationRequest(questionId, s, clarificationRequestDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == error_message
        clarificationRequestRepository.count() == 0L
        and: "the clarification request wasn't added to the student"
        def result = userRepository.findAll().get(0)
        result.getClarificationRequests().size() == 0

        where:
        content | is_question | has_answered || error_message
        ""      | true        | true         || ErrorMessage.CLARIFICATION_REQUEST_MISSING_CONTENT
        "    "  | true        | true         || ErrorMessage.CLARIFICATION_REQUEST_MISSING_CONTENT
        null    | true        | true         || ErrorMessage.CLARIFICATION_REQUEST_MISSING_CONTENT
        CONTENT | false       | true         || ErrorMessage.QUESTION_NOT_FOUND
        CONTENT | true        | false        || ErrorMessage.QUESTION_NOT_ANSWERED_BY_STUDENT
    }


    def changeStudent(boolean has_answered, User student2) {
        if (!has_answered) {
            return student2
        }
        return student
    }

    def changeQuestionId(boolean is_question) {
        if (!is_question) {
            questionId = INEXISTENT_QUESTION_ID
        }
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService();
        }
    }
}