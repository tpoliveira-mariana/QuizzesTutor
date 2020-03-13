package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequestAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import spock.lang.Ignore

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification


@DataJpaTest
class CheckClarificationRequestAnswerSpockTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"
    static final String CONTENT = "This is a test request."
    static final String USERNAME_ONE = "STUDENT_ONE"
    static final String USERNAME_TWO = "STUDENT_TWO"
    static final String NAME = "NAME"
    static final int INEXISTENT_QUESTION_ID = -1
    static final int INEXISTENT_USER_ID = -1
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
    QuizAnswerRepository quizAnswerRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    ClarificationRequestRepository clarificationRequestRepository

    @Autowired
    ClarificationService clarificationService

    @Autowired
    ClarificationRequestAnswerRepository clarificationRequestAnswerRepository

    def clarificationRequest
    def student
    def question
    def course
    def courseExecution
    def quiz
    def quizAnswer
    def quizQuestion
    def clarificationRequestDto


    def setup() {
        course = createCourse(COURSE_NAME)
        courseExecution = createCourseExecution(course, ACRONYM, ACADEMIC_TERM)
        quiz = createQuiz(KEY_ONE, courseExecution, Quiz.QuizType.GENERATED)
        question = createQuestion(KEY_ONE, course)
        quizQuestion = new QuizQuestion(quiz, question, 1)
        student = createStudent(new User(), KEY_ONE, NAME, USERNAME_ONE, courseExecution)
        quizAnswer = new QuizAnswer(student, quiz)

        clarificationRequestDto = new ClarificationRequestDto()
        clarificationRequestDto.setKey(KEY_ONE)
        clarificationRequestDto.setContent(CONTENT)
        clarificationRequest = new ClarificationRequest(student, question, clarificationRequestDto)

        courseRepository.save(course)
        courseExecutionRepository.save(courseExecution)
        quizRepository.save(quiz)
        questionRepository.save(question)
        quizQuestionRepository.save(quizQuestion)
        userRepository.save(student)
        quizAnswerRepository.save(quizAnswer)
        clarificationRequestRepository.save(clarificationRequest)
    }

    private User createStudent(User student, int key, String name, String username, CourseExecution courseExecution) {
        student.setKey(key)
        student.setName(name)
        student.setUsername(username)
        student.setRole(User.Role.STUDENT)
        student.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(student)
        return student
    }

    private Question createQuestion(int key, Course course) {
        def question = new Question()
        question.setKey(key)
        question.setCourse(course)
        course.addQuestion(question)
        return question
    }

    private Course createCourse(String name) {
        def course = new Course()
        course.setName(name)
        return course
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


    def "the student submitted the request, receives the answer"() {
        given: "a teacher"
        def teacher = new User()
        teacher.setKey(KEY_TWO)
        teacher.setUsername(USERNAME_TWO)
        teacher.addCourse(courseExecution)
        teacher.setRole(User.Role.TEACHER)
        userRepository.save(teacher)

        and: "his answer to the clarification request"
        def answer = new ClarificationRequestAnswer()
        answer.setCreator(teacher)
        answer.setRequest(clarificationRequest)
        answer.setContent(CONTENT)
        clarificationRequestAnswerRepository.save(answer)
        clarificationRequest.setAnswer(answer)
        clarificationRequestRepository.save(clarificationRequest)

        when:
        def result = clarificationService.getClarificationRequestAnswer(student.getId(), question.getId())

        then:"the correct answer is returned"
        result != null
        result.getContent() != null
        result.getCreator() == teacher.getId()
        result.getRequestId() == clarificationRequest.getId()
    }

    def "there is no answer available"() {
        when:
        def result = clarificationService.getClarificationRequestAnswer(student.getId(), question.getId())

        then: "no answer is returned"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == CLARIFICATION_REQUEST_UNANSWERED
    }

    def "the student didn't submit a clarification request for the question"() {
        given: "a student that didnt submit a clarification request"
        def student2 = createStudent(new User(), KEY_TWO, NAME, USERNAME_TWO, courseExecution)
        userRepository.save(student2)

        when:
        clarificationService.getClarificationRequestAnswer(student2.getId(), question.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == CLARIFICATION_REQUEST_NOT_SUBMITTED
    }

    def "the question doesn't exist"() {
        when:
        clarificationService.getClarificationRequestAnswer(student.getId(), INEXISTENT_QUESTION_ID)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == QUESTION_NOT_FOUND
    }

    def "the user doesn't exist"() {
        when:
        clarificationService.getClarificationRequestAnswer(INEXISTENT_USER_ID, question.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == USER_NOT_FOUND
    }

    def "the user isn't a student"() {
        given: "a user that is not a student"
        student.setRole(User.Role.TEACHER)
        userRepository.save(student)

        when:
        clarificationService.getClarificationRequestAnswer(student.getId(), question.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ACCESS_DENIED

    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService();
        }
    }
}