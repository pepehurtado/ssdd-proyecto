package es.um.sisdist.models;

import java.time.LocalDateTime;

public class PromptDTO {
        private String prompt;
        private String answer;
        private LocalDateTime timestamp;

        public PromptDTO() {
        }

        public PromptDTO(String prompt, String answer, LocalDateTime timestamp) {
            this.prompt = prompt;
            this.answer = answer;
            this.timestamp = timestamp;
        }
        
        public PromptDTO(String prompt, LocalDateTime timestamp) {
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

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    
}
