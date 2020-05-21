<template>
<div class="reports-index">

    <div class="reports-dropdown">
        <select v-model="selectedReport">
            <option v-for="report in availableReports"
                :key="report.name" 
                :value="report">{{report.name}}</option>
        </select>

        <div v-if="selectedReport">
            <div v-with="selectedReport">
                <select v-model="outputType">
                    <option v-for="out in selectedReport.outputTypes"
                        :key="out" 
                        :value="out">{{out}}</option>
                </select>
            </div>

            <ParametersControl report="selectedReport" />

            <button class="button is-success">Generate {{outputType}}</button>
        </div>
    </div>
</div>
</template>
<script>
import Axios from "axios";
import ParametersControl from "./ParametersControl.vue";

export default {
    components: { ParametersControl },
    data() {
        return {
            selectedReport:  null,
            outputType: 'None',
            availableReports: [],
            reportsLoaded: false
        };
    },

    methods: {
        fetchAvailableReports(evt) {
            evt.preventDefault();
            Axios.get("/api/reports")
              .then(response => {
                  this.availableReports = response.data;
              })
              .catch(err => {
                  console.log("Failed to load reports");
                  this.availableReports = [];
              });
            
            return false;
        }
    }
}
</script>