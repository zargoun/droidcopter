#pragma version(1)
#pragma rs java_package_name(org.byteguy.droidcopter.kinematics);

typedef struct SensorData {
    float3 accelerometer;
    float3 gyroscope;
    float3 magneticField;
} SensorData_t;

void root(const SensorData_t *sensorData, float4 *rotorSpeed) {
    rsDebug("ACCELEROMETER: ", sensorData[0].accelerometer[0]);
	rsDebug("GYROSCOPE: ", sensorData[0].gyroscope[0]);
	rsDebug("MAGNETIC_FIELD: ", sensorData[0].magneticField[0]);
	
	rotorSpeed[0] = sensorData[0].accelerometer[0];
	rotorSpeed[1] = sensorData[0].gyroscope[0];
	rotorSpeed[2] = sensorData[0].magneticField[0];
	rotorSpeed[3] = 0.0f;
}
