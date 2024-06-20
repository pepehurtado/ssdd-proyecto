package es.um.sisdist.backend.dao.models;

import java.time.LocalDateTime;

public class Prompt {
        private String prompt;
        private String answer;
        private LocalDateTime timestamp;

        public Prompt() {
        }

        public Prompt(String prompt, String answer, LocalDateTime timestamp) {
            this.prompt = prompt;
            this.answer = answer;
            this.timestamp = timestamp;
        }

        public Prompt(String prompt, LocalDateTime timestamp) {
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

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    
        @Override
        public String toString() {
            return "Prompt{" +
                    "prompt='" + prompt + '\'' +
                    ", answer='" + answer + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
}
