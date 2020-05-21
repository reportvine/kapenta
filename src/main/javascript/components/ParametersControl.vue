<template>
<div class="parameters-form">
    <div class="parameter-entry" :key="param.name" v-for="param in report.parameters">
        <label>{{param.name}}</label>
        <input type="text" :placeholder="'Enter a value here for ' + param.name">
        <em v-show="param.required">This parameter is required</em>
    </div>
</div>
</template>
<script>
export default {
    props: [ 'report' ]
}
</script>