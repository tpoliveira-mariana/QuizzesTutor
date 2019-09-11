package pt.ulisboa.tecnico.socialsoftware.tutor.statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswersDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ResultAnswersDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.SolvedQuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException.ExceptionError.NOT_ENOUGH_QUESTIONS;

@Service
public class StatementService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizService quizService;

    @Autowired
    private AnswerService answerService;

    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    public StatementQuizDto generateStudentQuiz(User user, int quizSize) {
        Quiz quiz = new Quiz();
        quiz.setNumber(quizService.getMaxQuizNumber() + 1);

        List<Question> activeQuestions = questionRepository.getActiveQuestions();

        if (activeQuestions.size() < quizSize) {
            throw new TutorException(NOT_ENOUGH_QUESTIONS, Integer.toString(activeQuestions.size()));
        }

        // TODO: to include knowhow about the student in the future
        quiz.generate(quizSize, activeQuestions);

        QuizAnswer quizAnswer = new QuizAnswer(user, quiz);

        entityManager.persist(quiz);
        entityManager.persist(quizAnswer);

        return new StatementQuizDto(quizAnswer);
    }

    @Transactional
    public List<StatementQuizDto> getAvailableQuizzes(User user) {

        LocalDateTime now = LocalDateTime.now();

        Set<Quiz> studentQuizzes =  user.getQuizAnswers().stream()
                .map(QuizAnswer::getQuiz)
                .filter(quiz -> quiz.getType().equals(Quiz.QuizType.TEACHER.name()))
                .collect(Collectors.toSet());

        quizRepository.findAvailableTeacherQuizzes(user.getYear()).stream()
                .filter(quiz -> quiz.getAvailableDate().isBefore(now) && !studentQuizzes.contains(quiz))
                .forEach(quiz ->  {
                    QuizAnswer quizAnswer = new QuizAnswer(user, quiz);
                    entityManager.persist(quizAnswer);
                });

        return user.getQuizAnswers().stream()
                .filter(quizAnswer -> !quizAnswer.getCompleted())
                .map(StatementQuizDto::new)
                .sorted(Comparator.comparing(StatementQuizDto::getAvailableDate))
                .collect(Collectors.toList());

    }

    @Transactional
    public List<SolvedQuizDto> getSolvedQuizzes(User user) {
        return user.getQuizAnswers().stream()
                .filter(quizAnswer -> quizAnswer.getCompleted())
                .map(SolvedQuizDto::new)
                .sorted(Comparator.comparing(SolvedQuizDto::getAnswerDate))
                .collect(Collectors.toList());
    }

    @Transactional
    public CorrectAnswersDto solveQuiz(User user, @Valid @RequestBody ResultAnswersDto answers) {
        return answerService.submitQuestionsAnswers(user, answers);
    }
}
