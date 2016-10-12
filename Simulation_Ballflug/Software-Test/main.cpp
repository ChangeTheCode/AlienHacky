#include <stdio.h>
#include <errno.h>

unsigned int iteration = 0;
unsigned int stubIteration = 0;

void getSenAcc(double* accArr){
    if (stubIteration < 80){
        accArr[0] = 0;
        accArr[1] = 0;
        accArr[2] = 0;
        stubIteration++;
        return;
    } else if (stubIteration < 100){
        accArr[0] = 3;
        accArr[1] = 0;
        accArr[2] = 0;
        stubIteration++;
        return;
    } else if (stubIteration < 140){
        accArr[0] = 0;
        accArr[1] = 0;
        accArr[2] = 0;
        stubIteration++;
        return;
    } else if (stubIteration < 160){
        accArr[0] = -3;
        accArr[1] = 0;
        accArr[2] = 0;
        stubIteration++;
        return;
    } else if (stubIteration >= 160){
        stubIteration = 0;
        return;
    }
}

#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCDFAInspection"
int main() {
    double pos[3] = {0};
    double speed[3] = {0};
    double acc[3] = {0};

    double A = -0.3; //Change of first derivative of state variable depending on the state variable itself
    double b = 0.5; //Change of the first derivative of state variable depending on input
    double c = 1; //Change of output depending on state variable
    double d = 0; //Change of output depending directly on the input

    double h = 0.02;

    double senAcc[3] = {0};

    FILE* f = fopen("computationOutput.txt", "w");
    if(f == NULL){
        printf("Shit happened; couldn't open file; errno = %d\n", errno);
        return -1;
    }

    while(1){
        getSenAcc(senAcc);

        acc[0] = A * speed[0] + b * senAcc[0];
        acc[1] = A * speed[1] + b * senAcc[1];
        acc[2] = A * speed[2] + b * senAcc[2];

        speed[0] = speed[0] + acc[0] * h;
        speed[1] = speed[1] + acc[1] * h;
        speed[2] = speed[2] + acc[2] * h;

        pos[0] = pos[0] + c * speed[0] + d * senAcc[0];
        pos[1] = pos[0] + c * speed[1] + d * senAcc[1];
        pos[2] = pos[0] + c * speed[2] + d * senAcc[2];

        fprintf(f, "\n\n---------------------------\nIteration No: %d\n", iteration);
        fprintf(f, "Position_x = %f\tPosition_y = %f\tPostion_z = %f\n", pos[0], pos[1], pos[2]);
        fprintf(f, "Speed_x = %f\tSpeed_y = %f\tSpeed_z = %f\n", speed[0], speed[1], speed[2]);
        fprintf(f, "Acc_x = %f\tAcc_y = %f\tAcc_z = %f\n", acc[0], acc[1], acc[2]);
        fprintf(f, "SenAcc_x = %f\tSenAcc_y = %f\tSenAcc_z = %f\n", senAcc[0], senAcc[1], senAcc[2]);
        fflush(f);

        iteration++;

        if(iteration > 400){
            break;
        }
    }

    fclose(f);
    return 0;
}
#pragma clang diagnostic pop