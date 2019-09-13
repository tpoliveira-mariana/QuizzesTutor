package pt.ulisboa.tecnico.socialsoftware.tutor.quiz;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class QuizDto implements Serializable {
    private Integer id;
    private Integer number;
    private Boolean scramble;
    private String title;
    private LocalDateTime creationDate;
    private LocalDateTime availableDate;
    private LocalDateTime conclusionDate;
    private Integer year;
    private String type;
    private Integer series;
    private String version;
    private int numberOfQuestions;
    private int numberOfAnswers;
    private List<QuestionDto> questions;

    public QuizDto(){
    }

    public QuizDto(Quiz quiz, boolean deepCopy) {
        this.id = quiz.getId();
        this.number = quiz.getNumber();
        this.scramble = quiz.getScramble();
        this.title = quiz.getTitle();
        this.creationDate = quiz.getCreationDate();
        this.availableDate = quiz.getAvailableDate();
        this.conclusionDate = quiz.getConclusionDate();
        this.availableDate = quiz.getAvailableDate();
        this.year = quiz.getYear();
        this.type = quiz.getType();
        this.series = quiz.getSeries();
        this.version = quiz.getVersion();
        this.numberOfQuestions = quiz.getQuizQuestions().size();
        this.numberOfAnswers = quiz.getQuizAnswers().size();
        if (deepCopy) {
            this.questions = quiz.getQuizQuestions().stream()
                    .sorted(Comparator.comparing(QuizQuestion::getSequence))
                    .map(quizQuestion -> {
                       QuestionDto questionDto = new QuestionDto(quizQuestion.getQuestion());
                       questionDto.setSequence(quizQuestion.getSequence());
                       return questionDto;
                    })
                    .collect(Collectors.toList());
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Boolean getScramble() {
        return scramble;
    }

    public void setScramble(Boolean scramble) {
        this.scramble = scramble;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(LocalDateTime conclusionDate) {
        this.conclusionDate = conclusionDate;
    }

    public LocalDateTime getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(LocalDateTime availableDate) {
        this.availableDate = availableDate;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(int numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public int getNumberOfAnswers() {
        return numberOfAnswers;
    }

    public void setNumberOfAnswers(int numberOfAnswers) {
        this.numberOfAnswers = numberOfAnswers;
    }

    public List<QuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDto> questions) {
        this.questions = questions;
    }
}