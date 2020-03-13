package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class TournamentService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto createTournament(int executionId, TournamentDto tournamentDto) {
        CourseExecution courseExecution = courseExecutionRepository.findById(executionId).orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND, executionId));

        User creator = userRepository.findByUsername(tournamentDto.getCreator().getUsername());

        Tournament tournament = new Tournament(tournamentDto);

        checkTopics(tournamentDto, courseExecution, tournament);

        checkCreatorCourseExecution(courseExecution, creator, tournament);

        tournamentDto.setParticipants(new ArrayList<>());

        addCreator(tournamentDto, creator, tournament);

        setCreationDate(tournamentDto, tournament);

        entityManager.persist(tournament);
        entityManager.persist(courseExecution);
        entityManager.persist(creator);
        courseExecution.addTournament(tournament);
        creator.addCreatedTournament(tournament);
        creator.addParticipantTournament(tournament);
        addTournamentToTopics(tournamentDto, courseExecution, tournament);
;
        return new TournamentDto(tournament);
    }

    private void addCreator(TournamentDto tournamentDto, User creator, Tournament tournament) {
        if (creator.getRole() == User.Role.STUDENT) {
            tournament.addParticipant(creator);
            tournamentDto.getParticipants().add(tournamentDto.getCreator());
        } else {
            throw new TutorException(TOURNAMENT_CREATED_BY_NON_STUDENT);
        }
    }

    private void setCreationDate(TournamentDto tournamentDto, Tournament tournament) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        if (tournamentDto.getCreationDate() == null) {
            LocalDateTime now = LocalDateTime.now();
            tournament.setCreationDate(now);
            tournamentDto.setCreationDate(now.format(formatter));
        } else {
            tournament.setCreationDate(LocalDateTime.parse(tournamentDto.getCreationDate(), formatter));
        }
    }

    private void checkCreatorCourseExecution(CourseExecution courseExecution, User creator, Tournament tournament) {
        if (!creator.getCourseExecutions().contains(courseExecution)) {
            throw new TutorException(USER_NOT_ENROLLED_IN_COURSE_EXECUTION, courseExecution.getAcronym());
        } else {
            tournament.setCreator(creator);
        }
    }

    private void checkTopics(TournamentDto tournamentDto, CourseExecution courseExecution, Tournament tournament) {
        for(TopicDto t: tournamentDto.getTopics()) {
            Topic topic = topicRepository.findTopicByName(courseExecution.getCourse().getId(), t.getName());
            if (topic == null) {
                throw new TutorException(TOPIC_NOT_FOUND, t.getId());
            } else {
                tournament.addTopic(topic);
            }
        }
    }

    private void addTournamentToTopics(TournamentDto tournamentDto, CourseExecution courseExecution, Tournament tournament){
        for(TopicDto t: tournamentDto.getTopics()) {
            Topic topic = topicRepository.findTopicByName(courseExecution.getCourse().getId(), t.getName());
            topic.addTournament(tournament);
            entityManager.persist(topic);
        }
    }
}
