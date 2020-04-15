<template>
  <v-card
    class="discussion"
    :max-height="270"
    style="margin-top: 30px;"
    outlined
  >
    <v-card-title class="title">
      Clarification Requests
      <v-btn
        v-if="!creatingRequest"
        class="add-button"
        dark
        color="primary"
        @click="newRequestButton()"
        data-cy="newRequest"
      >
        New Request
      </v-btn>
    </v-card-title>
    <v-divider></v-divider>
    <v-card-text v-if="creatingRequest">
      <v-text-field
        v-model="requestContent"
        label="Your request goes here."
        data-cy="inputRequest"
      ></v-text-field>
      <v-btn
        dark
        color="red"
        style="margin: 5px;"
        @click="cancelCreateRequest()"
      >
        Cancel
      </v-btn>
      <v-btn dark color="primary" style="margin: 5px;" @click="submitRequest()">
        Submit
      </v-btn>
    </v-card-text>

    <v-card-text v-else-if="hasClarificationRequests()">
      <v-expansion-panels focusable data-cy="questionRequests">
        <v-expansion-panel
          v-for="request in clarifications"
          :key="request.content"
        >
          <v-expansion-panel-header data-cy="requestHeader">
            {{ request.content }}
          </v-expansion-panel-header>
          <v-expansion-panel-content v-if="request.hasAnswer()">
            {{ request.answer }}
          </v-expansion-panel-content>
          <v-expansion-panel-content v-else>
            No answer available.
          </v-expansion-panel-content>
        </v-expansion-panel>
      </v-expansion-panels>
    </v-card-text>
    <v-card-text v-else>
      No requests available.
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import { Vue, Prop, Emit, Component } from 'vue-property-decorator';
import StatementQuestion from '@/models/statement/StatementQuestion';
import ClarificationRequest from '@/models/clarification/ClarificationRequest';

@Component
export default class DiscussionComponent extends Vue {
  @Prop(StatementQuestion) readonly question!: StatementQuestion;
  @Prop({ type: Array }) readonly clarifications!: ClarificationRequest[];

  creatingRequest: boolean = false;
  requestContent = '';
  nRequests!: number;

  newRequestButton(): void {
    this.creatingRequest = true;
  }

  cancelCreateRequest(): void {
    this.creatingRequest = false;
    this.requestContent = '';
  }

  hasClarificationRequests(): boolean {
    return this.clarifications.length > 0;
  }

  @Emit()
  submitRequest(): string[] {
    this.creatingRequest = false;
    let content = this.requestContent;
    this.requestContent = '';

    return [content, this.question.questionId.toString()];
  }
}
</script>

<style lang="scss" scoped>
.discussion {
  max-width: 1024px;
  perspective-origin: 512px 356.5px;
  transform-origin: 512px 356.5px;
  border: 0 none rgb(51, 51, 51);
  margin: 50px auto 150px;
  outline: rgb(51, 51, 51) none 0;
  overflow: hidden;
  top: -100px;
  letter-spacing: 0 !important;
  vertical-align: middle;

  .add-button {
    position: relative;
    right: -638px;
  }

  .title {
    height: 70px;
    text-align: left;
    text-decoration: none solid rgb(51, 51, 51);
    text-size-adjust: 100%;
  }
}
</style>