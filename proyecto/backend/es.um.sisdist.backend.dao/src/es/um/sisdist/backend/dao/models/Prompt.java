package es.um.sisdist.backend.dao.models;

import java.time.LocalDate;

public class Prompt {
        private String prompt;
        private String answer;
        private LocalDate timestamp;

        public Prompt() {
        }

        public Prompt(String prompt, String answer, LocalDate timestamp) {
            this.prompt = prompt;
            this.answer = answer;
            this.timestamp = timestamp;
        }

        public Prompt(String prompt, LocalDate timestamp) {
        this.timestamp = timestamp;
        this.prompt = prompt;
    }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public LocalDate getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDate timestamp) {
            this.timestamp = timestamp;
        }
    
}
