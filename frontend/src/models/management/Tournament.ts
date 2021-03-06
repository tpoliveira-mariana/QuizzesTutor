import User from '@/models/user/User';
import Topic from '@/models/management/Topic';
import { ISOtoString } from '@/services/ConvertDateService';

export default class Tournament {
  id: number | null = null;
  key!: number;
  title!: string;
  numberOfQuestions!: number;
  creationDate!: string | undefined;
  availableDate!: string | undefined;
  runningDate!: string | undefined;
  conclusionDate!: string | undefined;
  isCancelled: boolean = false;
  creator!: User;
  participants: User[] = [];
  topics: Topic[] = [];

  constructor(jsonObj?: Tournament) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.key = jsonObj.key;
      this.title = jsonObj.title;
      this.numberOfQuestions = jsonObj.numberOfQuestions;
      this.creationDate = ISOtoString(jsonObj.creationDate);
      this.availableDate = ISOtoString(jsonObj.availableDate);
      this.runningDate = ISOtoString(jsonObj.runningDate);
      this.conclusionDate = ISOtoString(jsonObj.conclusionDate);
      this.isCancelled = jsonObj.isCancelled;
      this.creator = new User(jsonObj.creator);

      if (jsonObj.participants) {
        this.participants = jsonObj.participants.map(
          (user: User) => new User(user)
        );
      }

      if (jsonObj.topics) {
        this.topics = jsonObj.topics.map((topic: Topic) => new Topic(topic));
      }
    }
  }
}
