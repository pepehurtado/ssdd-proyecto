package es.um.sisdist.models;

import java.time.LocalDate;

public class PromptDTO {
        private String prompt;
        private String answer;
        private LocalDate timestamp;

        public PromptDTO() {
        }

        public PromptDTO(String prompt, String answer, LocalDate timestamp) {
            this.prompt = prompt;
            this.answer = answer;
            this.timestamp = timestamp;
        }
        
        public PromptDTO(String prompt, LocalDate timestamp) {
        this.prompt = prompt;
        this.timestamp = timestamp;
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
