import { Component } from '@angular/core';
import { NavController, ModalController, ViewController } from 'ionic-angular';
import { Http, Headers } from '@angular/http';
import { QueriesPage } from '../queries/queries';
import { ChanelsPage } from '../chanels/chanels';
import { Observable } from 'rxjs/Rx';
import 'rxjs/Rx';

@Component({
    selector: 'page-home',
    templateUrl: 'home.html'
})
export class HomePage {
    user: any;
    validUser: boolean;
    constructor(public navCtrl: NavController, public modalCtrl: ModalController, public http: Http) {
        this.user = {};
        this.validUser = true;
    }

    login() {
        if (this.user.username == undefined || this.user.username == "" || this.user.password == undefined || this.user.password == "") {
            this.validUser = false;
            return;
        }

        let headers: Headers = new Headers();
        headers.set('Accept', 'text/plain');
        headers.set('Content-Type', 'application/json');
        headers.set('x-okapi-tenant', 'temp');
        this.http.post('http://ec2-52-15-97-36.us-east-2.compute.amazonaws.com:8084/login', this.user, {
            headers: headers
        }).subscribe(resp => {
            if (resp.ok) {
                this.navCtrl.push(ChanelsPage, {
                    user: this.user,
                    token: resp["_body"]
                });
            }
        },
        err => {
            if (err.status == 400) {
                this.validUser = false;
            }
        });
        //    .catch((error: any) => {
        //    if (error.status == 400) {
        //        this.validUser = false;                
        //    }

        //    return Observable.throw(new Error(error.status));
        //});        
    }

    openSignUpModal() {
        var modal = this.modalCtrl.create(SignUpModal, {});
        modal.present();
    }
}

@Component({
    template:`
    <ion-header>
        <ion-navbar color="primary">
            <ion-title text-center>
                Sign Up
            </ion-title>
            <ion-buttons end>
                <button ion-button icon-only (click)="dismiss()">
                    <ion-icon name="close"></ion-icon>    
                </button>
            </ion-buttons>
        </ion-navbar>
    </ion-header>
    <ion-content padding>
        <ion-list>
            <ion-item>
                <ion-input type="text" placeholder="Username" value="" [(ngModel)]="user.user_name"></ion-input>
            </ion-item>
            <ion-item>
                <ion-input type="password" placeholder="Password" value="" [(ngModel)]="user.password"></ion-input>
            </ion-item>
            <ion-item>
                <ion-input type="password" placeholder="Password Confirm" value="" [(ngModel)]="user.passConfirm"></ion-input>
            </ion-item>
            <ion-item>
                <ion-input type="text" placeholder="Age (Optional)" value="" [(ngModel)]="user.age"></ion-input>
            </ion-item>
        </ion-list>
            <ion-label>Gender (Optional): </ion-label>
            <ion-list radio-group [(ngModel)]="user.gender">
                    <ion-item>
                        <ion-label>Male</ion-label>
                        <ion-radio value="male"></ion-radio>
                    </ion-item>
                    <ion-item>
                  <ion-label>Female</ion-label>
                  <ion-radio value="female"></ion-radio>
               </ion-item> 
            </ion-list>
            <ion-label color="red" *ngIf="this.error == 'empty user'">Passwords don't match!</ion-label>
            <ion-label color="red" *ngIf="this.error == 'not match'">Passwords don't match!</ion-label>
            <ion-label color="red" *ngIf="this.error == 'empty'">Password cannot be empty!</ion-label>
            <button ion-button block (click)="signUp()">Sign Up</button>    
    </ion-content>

`
})
export class SignUpModal {
    user: any;
    error: string;

    constructor(public http: Http, public navCtrl: NavController, public viewCtrl: ViewController) {
        this.user = {};
        this.error = "";
    }

    signUp() {
        if (this.user.user_name == "") {
            this.error = "empty user";
            return;
        }

        if (this.user.password != this.user.passConfirm) {
            this.error = "not match";
            return;
        }

        if (this.user.password == "" || this.user.passConfirm == "" || this.user.password == undefined || this.user.passConfirm == undefined) {
            this.error = "empty";
            return;
        }

        if (this.user.age == undefined) {
            this.user.age = "";
        }

        if (this.user.gender == undefined) {
            this.user.gender = "";
        }

        this.user.member_channels = [];
        let userToSubmit: any = {};
        userToSubmit.username = this.user.user_name;
        userToSubmit.password = this.user.password;
        userToSubmit.user = {};
        userToSubmit.user.user_name = this.user.user_name;
        userToSubmit.user.age = this.user.age;
        userToSubmit.user.gender = this.user.gender;

        let headers: Headers = new Headers();
        headers.set('Accept', 'text/plain');
        headers.set('Content-Type', 'application/json');
        headers.set('x-okapi-tenant', 'temp');
        this.http.post('http://ec2-52-15-97-36.us-east-2.compute.amazonaws.com:8084/login', userToSubmit, {
            headers: headers
        }).subscribe(resp => {
            if (resp.ok) {
                this.navCtrl.push(ChanelsPage, {
                    user: userToSubmit,
                    //token: resp.
                });
            }
        });
    }

    dismiss() {
        this.viewCtrl.dismiss();
    }
}
