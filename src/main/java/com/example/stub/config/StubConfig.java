package com.example.stub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class StubConfig {

    @Value("${stub.config-file:config.yml}")
    private String configFile;

    private List<Rule> rules = new ArrayList<>();

    public List<Rule> getRules() {
        return rules;
    }

    @PostConstruct
    public void loadConfig() {
        try (InputStream in = new FileInputStream(configFile)) {
            Yaml yaml = new Yaml(new Constructor(StubConfigWrapper.class));
            StubConfigWrapper wrapper = yaml.load(in);
            if (wrapper != null && wrapper.getRules() != null) {
                this.rules = wrapper.getRules();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load stub config from " + configFile, e);
        }
    }

    public static class StubConfigWrapper {
        private List<Rule> rules;

        public List<Rule> getRules() {
            return rules;
        }

        public void setRules(List<Rule> rules) {
            this.rules = rules;
        }
    }
}