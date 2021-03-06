import axios from 'axios';
import Store from '@/store';
import Question from '@/models/management/Question';
import { Quiz } from '@/models/management/Quiz';
import Course from '@/models/user/Course';
import StatementCorrectAnswer from '@/models/statement/StatementCorrectAnswer';
import StudentStats from '@/models/statement/StudentStats';
import StatementQuiz from '@/models/statement/StatementQuiz';
import SolvedQuiz from '@/models/statement/SolvedQuiz';
import Topic from '@/models/management/Topic';
import { Student } from '@/models/management/Student';
import Assessment from '@/models/management/Assessment';
import AuthDto from '@/models/user/AuthDto';
import StatementAnswer from '@/models/statement/StatementAnswer';
import { QuizAnswers } from '@/models/management/QuizAnswers';
import StudentQuestion from '@/models/management/StudentQuestion';
import Tournament from '@/models/management/Tournament';
import ClarificationRequest from '@/models/clarification/ClarificationRequest';
import ClarificationMessage from '@/models/clarification/ClarificationMessage';
import UserNameCacheService from './UserNameCacheService';
import DashboardStats from '@/models/statement/DashboardStats';

const httpClient = axios.create();
httpClient.defaults.timeout = 10000;
httpClient.defaults.baseURL = process.env.VUE_APP_ROOT_API;
httpClient.defaults.withCredentials = true;
httpClient.defaults.headers.post['Content-Type'] = 'application/json';
httpClient.interceptors.request.use(
  config => {
    if (!config.headers.Authorization) {
      const token = Store.getters.getToken;

      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }

    return config;
  },
  error => Promise.reject(error)
);

export default class RemoteServices {
  static async fenixLogin(code: string): Promise<AuthDto> {
    return httpClient
      .get(`/auth/fenix?code=${code}`)
      .then(response => {
        const res = new AuthDto(response.data);
        UserNameCacheService.addUser(res.user);
        return res;
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async demoStudentLogin(): Promise<AuthDto> {
    return httpClient
      .get('/auth/demo/student')
      .then(response => {
        const res = new AuthDto(response.data);
        UserNameCacheService.addUser(res.user);
        return res;
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async demoTeacherLogin(): Promise<AuthDto> {
    return httpClient
      .get('/auth/demo/teacher')
      .then(response => {
        const res = new AuthDto(response.data);
        UserNameCacheService.addUser(res.user);
        return res;
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async demoAdminLogin(): Promise<AuthDto> {
    return httpClient
      .get('/auth/demo/admin')
      .then(response => {
        const res = new AuthDto(response.data);
        UserNameCacheService.addUser(res.user);
        return res;
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getUserStats(): Promise<StudentStats> {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/stats`
      )
      .then(response => {
        return new StudentStats(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  /*
   * Dashboard
   */

  static async getUserDashboardStats(userId: number): Promise<DashboardStats> {
    try {
      const response = await httpClient.get(
        `/courses/${Store.getters.getCurrentCourse.courseId}/dashboardStats/${userId}`
      );
      return new DashboardStats(response.data);
    } catch (error) {
      throw Error(await this.errorMessage(error));
    }
  }

  static async updateStatsVisibility(
    stats: DashboardStats
  ): Promise<DashboardStats> {
    try {
      const response = await httpClient.put(
        `/dashboardStats/${stats.id}`,
        stats
      );
      return new DashboardStats(response.data);
    } catch (error) {
      throw Error(await this.errorMessage(error));
    }
  }

  static async getQuestions(): Promise<Question[]> {
    return httpClient
      .get(`/courses/${Store.getters.getCurrentCourse.courseId}/questions`)
      .then(response => {
        return response.data.map((question: any) => {
          return new Question(question);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getQuestionById(id: number): Promise<Question> {
    try {
      const response = await httpClient.get(`/questions/${id}`);

      return new Question(response.data);
    } catch (error) {
      throw Error(await this.errorMessage(error));
    }
  }

  static async exportCourseQuestions(): Promise<Blob> {
    return httpClient
      .get(
        `/courses/${Store.getters.getCurrentCourse.courseId}/questions/export`,
        {
          responseType: 'blob'
        }
      )
      .then(response => {
        return new Blob([response.data], {
          type: 'application/zip, application/octet-stream'
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getAvailableQuestions(): Promise<Question[]> {
    return httpClient
      .get(
        `/courses/${Store.getters.getCurrentCourse.courseId}/questions/available`
      )
      .then(response => {
        return response.data.map((question: any) => {
          return new Question(question);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async createQuestion(question: Question): Promise<Question> {
    return httpClient
      .post(
        `/courses/${Store.getters.getCurrentCourse.courseId}/questions/`,
        question
      )
      .then(response => {
        return new Question(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async updateQuestion(question: Question): Promise<Question> {
    return httpClient
      .put(`/questions/${question.id}`, question)
      .then(response => {
        return new Question(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async deleteQuestion(questionId: number) {
    return httpClient.delete(`/questions/${questionId}`).catch(async error => {
      throw Error(await this.errorMessage(error));
    });
  }

  static async setQuestionStatus(
    questionId: number,
    status: String
  ): Promise<Question> {
    return httpClient
      .post(`/questions/${questionId}/set-status`, status, {})
      .then(response => {
        return new Question(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async uploadImage(file: File, questionId: number): Promise<string> {
    let formData = new FormData();
    formData.append('file', file);
    return httpClient
      .put(`/questions/${questionId}/image`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      .then(response => {
        return response.data as string;
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async updateQuestionTopics(questionId: number, topics: Topic[]) {
    return httpClient.put(`/questions/${questionId}/topics`, topics);
  }

  static async getTopics(): Promise<Topic[]> {
    return httpClient
      .get(`/courses/${Store.getters.getCurrentCourse.courseId}/topics`)
      .then(response => {
        return response.data.map((topic: any) => {
          return new Topic(topic);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  /*
   * Student Questions
   */

  static async getStudentQuestionStatuses(): Promise<StudentQuestion[]> {
    return httpClient
      .get(
        `/courses/${Store.getters.getCurrentCourse.courseId}/studentQuestions/checkStatus`
      )
      .then(response => {
        return response.data.map((studentQuestion: any) => {
          return new StudentQuestion(studentQuestion);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async evaluateStudentQuestion(
    questionId: number,
    status: string,
    justification: String
  ): Promise<StudentQuestion> {
    try {
      const response = await httpClient.post(
        `/courses/${Store.getters.getCurrentCourse.courseId}/studentQuestions/${questionId}/evaluate`,
        {
          evaluation: StudentQuestion.getServerStatusFormat(status),
          justification: justification.trim() === '' ? null : justification
        }
      );
      return new StudentQuestion(response.data);
    } catch (error) {
      throw Error(await this.errorMessage(error));
    }
  }

  static async editAndPromoteStudentQuestion(
    studentQuestion: StudentQuestion
  ): Promise<StudentQuestion> {
    try {
      const response = await httpClient.put(
        `/courses/${Store.getters.getCurrentCourse.courseId}/studentQuestions/${studentQuestion.id}/evaluate`,
        StudentQuestion.toRequest(studentQuestion)
      );
      return new StudentQuestion(response.data);
    } catch (error) {
      throw Error(await this.errorMessage(error));
    }
  }

  static async getSubmittedStudentQuestions(): Promise<StudentQuestion[]> {
    try {
      const response = await httpClient.get(
        `/courses/${Store.getters.getCurrentCourse.courseId}/studentQuestions`
      );
      return response.data.map((studentQuestion: any) => {
        return new StudentQuestion(studentQuestion);
      });
    } catch (error) {
      throw Error(await this.errorMessage(error));
    }
  }

  static async createStudentQuestion(
    studentQuestion: StudentQuestion
  ): Promise<StudentQuestion> {
    return httpClient
      .post(
        `/courses/${Store.getters.getCurrentCourse.courseId}/studentQuestions`,
        StudentQuestion.toRequest(studentQuestion)
      )
      .then(response => {
        return new StudentQuestion(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async updateStudentQuestion(
    studentQuestion: StudentQuestion
  ): Promise<StudentQuestion> {
    return httpClient
      .put(
        `/courses/${Store.getters.getCurrentCourse.courseId}/studentQuestions/${studentQuestion.id}`,
        StudentQuestion.toRequest(studentQuestion)
      )
      .then(response => {
        return new StudentQuestion(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async deleteStudentQuestion(studentQuestionId: number) {
    return httpClient
      .delete(
        `/courses/${Store.getters.getCurrentCourse.courseId}/studentQuestions/${studentQuestionId}`
      )
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getAvailableTournaments(): Promise<Tournament[]> {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/tournaments/available`
      )
      .then(response => {
        return response.data.map((tournament: any) => {
          return new Tournament(tournament);
        });
      })
      .catch(async error => {
        console.log(this.errorMessage(error));
      });
  }

  static async getSignedUpRunningTournaments(): Promise<Tournament[]> {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/tournaments/running/signed-up`
      )
      .then(response => {
        return response.data.map((tournament: any) => {
          return new Tournament(tournament);
        });
      })
      .catch(async error => {
        console.log(this.errorMessage(error));
      });
  }

  static async getCreatedTournaments(): Promise<Tournament[]> {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/tournaments/created`
      )
      .then(response => {
        return response.data.map((tournament: any) => {
          return new Tournament(tournament);
        });
      })
      .catch(async error => {
        console.log(this.errorMessage(error));
      });
  }

  static async getSolvedTournaments(): Promise<Tournament[]> {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/tournaments/solved`
      )
      .then(response => {
        return response.data.map((tournament: any) => {
          return new Tournament(tournament);
        });
      })
      .catch(async error => {
        console.log(this.errorMessage(error));
      });
  }

  static async createTournament(tournament: Tournament): Promise<Tournament> {
    try {
      const response = await httpClient.post(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/tournaments`,
        tournament
      );
      return new Tournament(response.data);
    } catch (error) {
      throw Error(await this.errorMessage(error));
    }
  }

  static async signUpInTournament(tournamentId: number) {
    try {
      await httpClient.post(`/tournaments/${tournamentId}/sign-up`);
    } catch (error) {
      throw Error(await this.errorMessage(error));
    }
  }

  static async deleteTournament(tournament: Tournament) {
    try {
      await httpClient.delete(`/tournaments/${tournament.id}`);
    } catch (error) {
      throw Error(await this.errorMessage(error));
    }
  }

  static async getTournamentQuiz(
    tournament: Tournament
  ): Promise<StatementQuiz> {
    try {
      return (await httpClient.get(`/tournaments/${tournament.id}/quiz`)).data;
    } catch (error) {
      throw Error(await this.errorMessage(error));
    }
  }

  static async cancelTournament(tournament: Tournament) {
    try {
      await httpClient.post(`/tournaments/${tournament.id}/cancel`);
    } catch (error) {
      throw Error(await this.errorMessage(error));
    }
  }

  static getAvailableQuizzes(): Promise<StatementQuiz[]> {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/quizzes/available`
      )
      .then(response => {
        return response.data.map((statementQuiz: any) => {
          return new StatementQuiz(statementQuiz);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async generateStatementQuiz(params: object): Promise<StatementQuiz> {
    return httpClient
      .post(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/quizzes/generate`,
        params
      )
      .then(response => {
        return new StatementQuiz(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getSolvedQuizzes(): Promise<SolvedQuiz[]> {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/quizzes/solved`
      )
      .then(response => {
        return response.data.map((solvedQuiz: any) => {
          return new SolvedQuiz(solvedQuiz);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getQuizByQRCode(quizId: number): Promise<StatementQuiz> {
    return httpClient
      .get(`/quizzes/${quizId}/byqrcode`)
      .then(response => {
        return new StatementQuiz(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async exportQuiz(quizId: number): Promise<Blob> {
    return httpClient
      .get(`/quizzes/${quizId}/export`, {
        responseType: 'blob'
      })
      .then(response => {
        return new Blob([response.data], {
          type: 'application/zip, application/octet-stream'
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async startQuiz(quizId: number) {
    return httpClient.get(`/quizzes/${quizId}/start`).catch(async error => {
      throw Error(await this.errorMessage(error));
    });
  }

  static async submitAnswer(quizId: number, answer: StatementAnswer) {
    return httpClient
      .post(`/quizzes/${quizId}/submit`, answer)
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async concludeQuiz(
    quizId: number
  ): Promise<StatementCorrectAnswer[] | void> {
    return httpClient
      .get(`/quizzes/${quizId}/conclude`)
      .then(response => {
        if (response.data) {
          return response.data.map((answer: any) => {
            return new StatementCorrectAnswer(answer);
          });
        }
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async createTopic(topic: Topic): Promise<Topic> {
    return httpClient
      .post(
        `/courses/${Store.getters.getCurrentCourse.courseId}/topics/`,
        topic
      )
      .then(response => {
        return new Topic(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async updateTopic(topic: Topic): Promise<Topic> {
    return httpClient
      .put(`/topics/${topic.id}`, topic)
      .then(response => {
        return new Topic(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async deleteTopic(topic: Topic) {
    return httpClient.delete(`/topics/${topic.id}`).catch(async error => {
      throw Error(await this.errorMessage(error));
    });
  }

  static async getNonGeneratedQuizzes(): Promise<Quiz[]> {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/quizzes/non-generated`
      )
      .then(response => {
        return response.data.map((quiz: any) => {
          return new Quiz(quiz);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async deleteQuiz(quizId: number) {
    return httpClient.delete(`/quizzes/${quizId}`).catch(async error => {
      throw Error(await this.errorMessage(error));
    });
  }

  static async getQuiz(quizId: number): Promise<Quiz> {
    return httpClient
      .get(`/quizzes/${quizId}`)
      .then(response => {
        return new Quiz(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getQuizAnswers(quizId: number): Promise<QuizAnswers> {
    return httpClient
      .get(`/quizzes/${quizId}/answers`)
      .then(response => {
        return new QuizAnswers(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async saveQuiz(quiz: Quiz): Promise<Quiz> {
    if (quiz.id) {
      return httpClient
        .put(`/quizzes/${quiz.id}`, quiz)
        .then(response => {
          return new Quiz(response.data);
        })
        .catch(async error => {
          throw Error(await this.errorMessage(error));
        });
    } else {
      return httpClient
        .post(
          `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/quizzes`,
          quiz
        )
        .then(response => {
          return new Quiz(response.data);
        })
        .catch(async error => {
          throw Error(await this.errorMessage(error));
        });
    }
  }

  static async getCourseStudents(course: Course) {
    return httpClient
      .get(`/executions/${course.courseExecutionId}/students`)
      .then(response => {
        return response.data.map((student: any) => {
          return new Student(student);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getAssessments(): Promise<Assessment[]> {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/assessments`
      )
      .then(response => {
        return response.data.map((assessment: any) => {
          return new Assessment(assessment);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async getAvailableAssessments() {
    return httpClient
      .get(
        `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/assessments/available`
      )
      .then(response => {
        return response.data.map((assessment: any) => {
          return new Assessment(assessment);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async saveAssessment(assessment: Assessment) {
    if (assessment.id) {
      return httpClient
        .put(`/assessments/${assessment.id}`, assessment)
        .then(response => {
          return new Assessment(response.data);
        })
        .catch(async error => {
          throw Error(await this.errorMessage(error));
        });
    } else {
      return httpClient
        .post(
          `/executions/${Store.getters.getCurrentCourse.courseExecutionId}/assessments`,
          assessment
        )
        .then(response => {
          return new Assessment(response.data);
        })
        .catch(async error => {
          throw Error(await this.errorMessage(error));
        });
    }
  }

  static async deleteAssessment(assessmentId: number) {
    return httpClient
      .delete(`/assessments/${assessmentId}`)
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async setAssessmentStatus(
    assessmentId: number,
    status: string
  ): Promise<Assessment> {
    return httpClient
      .post(`/assessments/${assessmentId}/set-status`, status, {
        headers: {
          'Content-Type': 'text/html'
        }
      })
      .then(response => {
        return new Assessment(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static getCourses(): Promise<Course[]> {
    return httpClient
      .get('/courses/executions')
      .then(response => {
        return response.data.map((course: any) => {
          return new Course(course);
        });
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async activateCourse(course: Course): Promise<Course> {
    return httpClient
      .post('/courses/activate', course)
      .then(response => {
        return new Course(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async createExternalCourse(course: Course): Promise<Course> {
    return httpClient
      .post('/courses/external', course)
      .then(response => {
        return new Course(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async deleteCourse(courseExecutionId: number | undefined) {
    return httpClient
      .delete(`/executions/${courseExecutionId}`)
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async exportAll() {
    return httpClient
      .get('/admin/export', {
        responseType: 'blob'
      })
      .then(response => {
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        let dateTime = new Date();
        link.setAttribute(
          'download',
          `export-${dateTime.toLocaleString()}.zip`
        );
        document.body.appendChild(link);
        link.click();
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async errorMessage(error: any): Promise<string> {
    if (error.message === 'Network Error') {
      return 'Unable to connect to server';
    } else if (error.message.split(' ')[0] === 'timeout') {
      return 'Request timeout - Server took too long to respond';
    } else if (error.response) {
      return error.response.data.message;
    } else if (error.message === 'Request failed with status code 403') {
      await Store.dispatch('logout');
      return 'Unauthorized access or Expired token';
    } else {
      console.log(error);
      return 'Unknown Error - Contact admin';
    }
  }

  static async submitClarificationRequest(
    clarificationRequest: ClarificationRequest
  ): Promise<ClarificationRequest> {
    return httpClient
      .post(
        `/student/results/questions/${clarificationRequest.getQuestionId()}/clarifications`,
        clarificationRequest
      )
      .then(response => {
        return new ClarificationRequest(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }

  static async submitClarificationMessage(
    reqId: number,
    content: string,
    resolved: boolean = null
  ): Promise<ClarificationMessage> {
    if (content.trim() == '') {
      // eslint-disable-next-line
      throw Error("Message can't be empty");
    }

    try {
      const response = await httpClient.post(
        `/clarifications/${reqId}/messages`,
        { content, resolved }
      );

      return new ClarificationMessage(response.data);
    } catch (err) {
      throw Error(await this.errorMessage(err));
    }
  }

  static async getUserClarificationRequests(): Promise<ClarificationRequest[]> {
    try {
      const response = await httpClient.get('/clarifications');

      UserNameCacheService.bulkAdd(response.data.names);

      return response.data.requests.map(
        (req: ClarificationRequest) => new ClarificationRequest(req)
      );
    } catch (error) {
      throw Error(await this.errorMessage(error));
    }
  }

  static async deleteClarificationRequest(id: number) {
    return httpClient.delete(`/clarifications/${id}`).catch(async error => {
      throw Error(await this.errorMessage(error));
    });
  }

  static async deleteClarificationMessage(
    msg: ClarificationMessage
  ): Promise<void> {
    try {
      await httpClient.delete(`/clarifications/messages/${msg.getId()}`);
    } catch (err) {
      throw Error(await this.errorMessage(err));
    }
  }

  static async changeClarificationRequestStatus(
    id: number,
    status: string
  ): Promise<ClarificationRequest> {
    return httpClient
      .put(`/clarifications/${id}/status`, status, {
        headers: {
          'Content-Type': 'text/plain'
        }
      })
      .then(response => {
        return new ClarificationRequest(response.data);
      })
      .catch(async error => {
        throw Error(await this.errorMessage(error));
      });
  }
}
