import Question from './Question';

export default class StudentQuestion extends Question {
  submittedStatus: string = 'WAITING_FOR_APPROVAL';
  justification: string = '';
  username!: string;

  constructor(jsonObj?: StudentQuestion) {
    super(jsonObj);
    this.status = 'DISABLED';
    if (jsonObj) {
      this.submittedStatus = this.getSubmittedStatus(jsonObj.submittedStatus);
      this.justification = jsonObj.justification;
      this.username = jsonObj.username;
      this.status = jsonObj.status;
    }
  }

  getSubmittedStatus(submittedStatus: string): string {
    if (submittedStatus === 'APPROVED') return 'Approved';
    if (submittedStatus === 'REJECTED') return 'Rejected';
    return 'Waiting for Approval';
  }
}
